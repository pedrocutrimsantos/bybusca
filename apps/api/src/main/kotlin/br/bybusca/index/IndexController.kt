package br.bybusca.index

import br.bybusca.auth.AuthService
import br.bybusca.common.ApiException
import br.bybusca.domain.TenantRepository
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import org.springframework.http.HttpStatus
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.web.bind.annotation.*
import java.time.Instant
import java.util.UUID

data class IndexDocumentRequest(
    @field:NotBlank val externalId: String,
    @field:NotBlank val type: String,
    @field:NotBlank val title: String,
    val body: String = "",
    val url: String = "",
    val visibility: String = "network",
    val allowedRoles: List<String> = emptyList(),
    val allowedClassIds: List<String> = emptyList(),
    val allowedSubjectIds: List<String> = emptyList(),
    val metadata: Map<String, Any?> = emptyMap()
)

data class IndexDocumentResponse(
    val id: UUID,
    val externalId: String,
    val indexedAt: Instant
)

@RestController
@RequestMapping("/api/v1/index")
class IndexController(
    private val authService: AuthService,
    private val tenants: TenantRepository,
    private val jdbc: JdbcTemplate
) {
    @PostMapping("/documents")
    fun upsert(
        @RequestHeader(value = "Authorization", required = false) authorization: String?,
        @RequestHeader(value = "X-Tenant", required = false) tenantHeader: String?,
        @Valid @RequestBody body: IndexDocumentRequest
    ): IndexDocumentResponse {
        val principal = authService.parsePrincipal(authorization, tenantHeader)
        if (!principal.roles.any { it in setOf("ADMIN", "COORDINATOR", "TEACHER") }) {
            throw ApiException("FORBIDDEN", "Sem permissão para indexar", HttpStatus.FORBIDDEN)
        }
        val tenant = tenants.findByCode(principal.tenantCode)
            ?: throw ApiException("TENANT_NOT_FOUND", "Escola não encontrada", HttpStatus.NOT_FOUND)

        val id = UUID.randomUUID()
        val indexedAt = Instant.now()
        val metaJson = com.fasterxml.jackson.module.kotlin.jacksonObjectMapper().writeValueAsString(body.metadata)

        jdbc.update({ connection ->
            val sql = """
                INSERT INTO documents (id, tenant_id, external_id, type, title, body, url, visibility, metadata, search_vector, indexed_at, deleted_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?::jsonb,
                  setweight(to_tsvector('portuguese_unaccent', coalesce(?, '')), 'A') ||
                  setweight(to_tsvector('portuguese_unaccent', coalesce(?, '')), 'B'),
                  ?, NULL)
                ON CONFLICT (tenant_id, external_id) DO UPDATE SET
                  type = EXCLUDED.type,
                  title = EXCLUDED.title,
                  body = EXCLUDED.body,
                  url = EXCLUDED.url,
                  visibility = EXCLUDED.visibility,
                  metadata = EXCLUDED.metadata,
                  search_vector = EXCLUDED.search_vector,
                  indexed_at = EXCLUDED.indexed_at,
                  deleted_at = NULL
            """.trimIndent()
            val ps = connection.prepareStatement(sql)
            var i = 1
            ps.setObject(i++, id)
            ps.setObject(i++, tenant.id)
            ps.setString(i++, body.externalId)
            ps.setString(i++, body.type.uppercase())
            ps.setString(i++, body.title)
            ps.setString(i++, body.body)
            ps.setString(i++, body.url)
            ps.setString(i++, body.visibility.lowercase())
            ps.setString(i++, metaJson)
            ps.setString(i++, body.title)
            ps.setString(i++, body.body)
            ps.setObject(i, java.sql.Timestamp.from(indexedAt))
            ps
        })

        val savedId = jdbc.queryForObject(
            "SELECT id FROM documents WHERE tenant_id = ? AND external_id = ?",
            UUID::class.java,
            tenant.id,
            body.externalId
        )!!

        jdbc.update({ connection ->
            val sql = """
                INSERT INTO document_acl (document_id, allowed_roles, allowed_class_ids, allowed_subject_ids)
                VALUES (?, ?, ?, ?)
                ON CONFLICT (document_id) DO UPDATE SET
                  allowed_roles = EXCLUDED.allowed_roles,
                  allowed_class_ids = EXCLUDED.allowed_class_ids,
                  allowed_subject_ids = EXCLUDED.allowed_subject_ids
            """.trimIndent()
            val ps = connection.prepareStatement(sql)
            ps.setObject(1, savedId)
            ps.setArray(2, connection.createArrayOf("text", body.allowedRoles.toTypedArray()))
            ps.setArray(3, connection.createArrayOf("text", body.allowedClassIds.toTypedArray()))
            ps.setArray(4, connection.createArrayOf("text", body.allowedSubjectIds.toTypedArray()))
            ps
        })

        return IndexDocumentResponse(savedId, body.externalId, indexedAt)
    }

    @PostMapping("/reindex")
    fun reindex(
        @RequestHeader(value = "Authorization", required = false) authorization: String?,
        @RequestHeader(value = "X-Tenant", required = false) tenantHeader: String?
    ): Map<String, Any?> {
        val principal = authService.parsePrincipal(authorization, tenantHeader)
        if (!principal.roles.any { it in setOf("ADMIN", "COORDINATOR") }) {
            throw ApiException("FORBIDDEN", "Sem permissão para reindexar", HttpStatus.FORBIDDEN)
        }
        val tenant = tenants.findByCode(principal.tenantCode)
            ?: throw ApiException("TENANT_NOT_FOUND", "Escola não encontrada", HttpStatus.NOT_FOUND)

        val started = System.currentTimeMillis()
        val updated = jdbc.update(
            """
            UPDATE documents SET
              search_vector =
                setweight(to_tsvector('portuguese_unaccent', coalesce(title, '')), 'A') ||
                setweight(to_tsvector('portuguese_unaccent', coalesce(body, '')), 'B'),
              indexed_at = NOW()
            WHERE tenant_id = ? AND deleted_at IS NULL
            """.trimIndent(),
            tenant.id
        )
        return mapOf(
            "tenant" to tenant.code,
            "updated" to updated,
            "tookMs" to (System.currentTimeMillis() - started)
        )
    }
}
