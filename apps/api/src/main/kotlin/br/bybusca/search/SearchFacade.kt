package br.bybusca.search

import br.bybusca.acl.AclService
import br.bybusca.acl.SearchPrincipal
import br.bybusca.domain.TenantRepository
import br.bybusca.pse.PseSearchService
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.sql.ResultSet

data class SearchHit(
    val id: String,
    val type: String,
    val title: String,
    val snippet: String,
    val url: String,
    val score: Double,
    val source: String
)

data class SearchResponse(
    val items: List<SearchHit>,
    val page: Int,
    val size: Int,
    val total: Long,
    val tookMs: Long,
    val warnings: List<String>
)

data class SuggestItem(val text: String, val type: String)

@Service
class SearchFacade(
    private val jdbc: JdbcTemplate,
    private val tenants: TenantRepository,
    private val acl: AclService,
    private val pse: PseSearchService
) {
    @Transactional(readOnly = true)
    fun search(
        principal: SearchPrincipal,
        q: String,
        types: List<String>?,
        page: Int,
        size: Int,
        includeWeb: Boolean
    ): SearchResponse {
        val started = System.currentTimeMillis()
        val tenant = tenants.findByCode(principal.tenantCode)
            ?: return SearchResponse(emptyList(), page, size, 0, 0, listOf("TENANT_NOT_FOUND"))

        val safePage = page.coerceAtLeast(0)
        val safeSize = size.coerceIn(1, 50)
        // websearch_to_tsquery + portuguese_unaccent: UX tipo Google (acentos/espaços)
        val rawQuery = q.trim().replace(Regex("[|&!:*()<>']"), " ").replace(Regex("\\s+"), " ").trim()
        if (rawQuery.isBlank()) {
            return SearchResponse(emptyList(), safePage, safeSize, 0, 0, emptyList())
        }

        val visibility = if (principal.isGuest) {
            " AND d.visibility IN ('public','network') "
        } else {
            " AND ${acl.sqlVisibilityClause("d")} "
        }
        val typeFilter = if (!types.isNullOrEmpty()) " AND d.type = ANY(?) " else ""

        val baseFrom = """
            FROM documents d
            WHERE d.tenant_id = ?
              AND d.deleted_at IS NULL
              AND d.search_vector @@ websearch_to_tsquery('portuguese_unaccent', ?)
              $visibility
              $typeFilter
        """.trimIndent()

        val countSql = "SELECT COUNT(*) $baseFrom"
        val listSql = """
            SELECT d.id::text, d.type, d.title, d.body, d.url,
                   ts_rank_cd(d.search_vector, websearch_to_tsquery('portuguese_unaccent', ?)) AS rank
            $baseFrom
            ORDER BY rank DESC
            LIMIT ? OFFSET ?
        """.trimIndent()

        val total = jdbc.query({ connection ->
            val ps = connection.prepareStatement(countSql)
            bindCommon(ps, tenant.id, rawQuery, principal, types, startIndex = 1)
            ps
        }) { rs: ResultSet, _: Int -> rs.getLong(1) }.firstOrNull() ?: 0L

        val internal = jdbc.query({ connection ->
            val ps = connection.prepareStatement(listSql)
            var i = 1
            ps.setString(i++, rawQuery) // rank query
            i = bindCommon(ps, tenant.id, rawQuery, principal, types, startIndex = i)
            ps.setInt(i++, safeSize)
            ps.setInt(i, safePage * safeSize)
            ps
        }) { rs, _ ->
            val title = rs.getString("title").orEmpty()
            val body = rs.getString("body").orEmpty()
            SearchHit(
                id = rs.getString(1),
                type = rs.getString("type"),
                title = title,
                snippet = buildSnippet(q, "$title. $body"),
                url = rs.getString("url").orEmpty(),
                score = rs.getDouble("rank"),
                source = "internal"
            )
        }

        val warnings = mutableListOf<String>()
        val web = if (includeWeb) {
            val (hits, warn) = pse.search(q, minOf(5, safeSize))
            if (warn != null) warnings += warn
            hits
        } else emptyList()

        return SearchResponse(
            items = (internal + web).sortedByDescending { it.score },
            page = safePage,
            size = safeSize,
            total = total + web.size,
            tookMs = System.currentTimeMillis() - started,
            warnings = warnings
        )
    }

    @Transactional(readOnly = true)
    fun suggest(principal: SearchPrincipal, q: String): List<SuggestItem> {
        val tenant = tenants.findByCode(principal.tenantCode) ?: return emptyList()
        val prefix = q.trim()
        if (prefix.length < 2) return emptyList()
        return jdbc.query(
            """
            SELECT d.title, d.type FROM documents d
            WHERE d.tenant_id = ? AND d.deleted_at IS NULL
              AND d.visibility IN ('public','network')
              AND lower(d.title) LIKE lower(?)
            ORDER BY d.title ASC LIMIT 8
            """.trimIndent(),
            { rs, _ -> SuggestItem(rs.getString(1), rs.getString(2)) },
            tenant.id,
            "$prefix%"
        )
    }

    private fun bindCommon(
        ps: java.sql.PreparedStatement,
        tenantId: java.util.UUID,
        tsQuery: String,
        principal: SearchPrincipal,
        types: List<String>?,
        startIndex: Int
    ): Int {
        var i = startIndex
        ps.setObject(i++, tenantId)
        ps.setString(i++, tsQuery)
        if (!principal.isGuest) {
            val roles = principal.roles.toTypedArray()
            val classIds = principal.classIds.toTypedArray()
            val subjectIds = principal.subjectIds.toTypedArray()
            ps.setArray(i++, ps.connection.createArrayOf("text", roles))
            ps.setArray(i++, ps.connection.createArrayOf("text", classIds))
            ps.setArray(i++, ps.connection.createArrayOf("text", subjectIds))
        }
        if (!types.isNullOrEmpty()) {
            ps.setArray(i++, ps.connection.createArrayOf("text", types.toTypedArray()))
        }
        return i
    }

    private fun buildSnippet(q: String, text: String): String {
        val plain = text.replace(Regex("\\s+"), " ").trim()
        val terms = q.lowercase().split(Regex("\\s+")).filter { it.length > 1 }
        var snippet = if (plain.length <= 240) plain else plain.take(237) + "..."
        for (t in terms) {
            snippet = snippet.replace(Regex("(?i)(${Regex.escape(t)})"), "<mark>$1</mark>")
        }
        return snippet
    }
}
