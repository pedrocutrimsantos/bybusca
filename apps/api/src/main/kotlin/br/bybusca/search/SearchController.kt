package br.bybusca.search

import br.bybusca.auth.AuthService
import br.bybusca.common.ApiException
import br.bybusca.domain.DocumentRepository
import br.bybusca.domain.TenantRepository
import jakarta.servlet.http.HttpServletRequest
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1")
class SearchController(
    private val searchFacade: SearchFacade,
    private val authService: AuthService,
    private val tenants: TenantRepository,
    private val documents: DocumentRepository,
    private val jdbc: JdbcTemplate
) {
    @GetMapping("/search")
    fun search(
        @RequestParam q: String?,
        @RequestParam(required = false) types: String?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(defaultValue = "false") includeWeb: Boolean,
        @RequestHeader(value = "Authorization", required = false) authorization: String?,
        @RequestHeader(value = "X-Tenant", required = false) tenant: String?,
        request: HttpServletRequest
    ): SearchResponse {
        val query = q?.trim().orEmpty()
        if (query.isEmpty()) throw ApiException("VALIDATION_ERROR", "Informe o termo de busca (q)")
        val principal = authService.parsePrincipal(authorization, tenant)
        val typeList = types?.split(",")?.map { it.trim().uppercase() }?.filter { it.isNotBlank() }
        val result = searchFacade.search(principal, query, typeList, page, size, includeWeb)
        audit(principal.tenantCode, principal.userId, query, request.remoteAddr, result)
        return result
    }

    @GetMapping("/suggest")
    fun suggest(
        @RequestParam q: String?,
        @RequestHeader(value = "Authorization", required = false) authorization: String?,
        @RequestHeader(value = "X-Tenant", required = false) tenant: String?
    ): List<SuggestItem> {
        val principal = authService.parsePrincipal(authorization, tenant)
        return searchFacade.suggest(principal, q.orEmpty())
    }

    @GetMapping("/admin/health-index")
    fun healthIndex(
        @RequestHeader(value = "Authorization", required = false) authorization: String?,
        @RequestHeader(value = "X-Tenant", required = false) tenant: String?
    ): Map<String, Any?> {
        val principal = authService.parsePrincipal(authorization, tenant)
        if (!principal.roles.any { it in setOf("ADMIN", "COORDINATOR") }) {
            throw ApiException("FORBIDDEN", "Acesso restrito à administração", org.springframework.http.HttpStatus.FORBIDDEN)
        }
        val t = tenants.findByCode(principal.tenantCode)
            ?: throw ApiException("TENANT_NOT_FOUND", "Escola não encontrada", org.springframework.http.HttpStatus.NOT_FOUND)
        return mapOf(
            "tenant" to t.code,
            "documents" to documents.countActive(t.id),
            "status" to "UP"
        )
    }

    private fun audit(
        tenantCode: String,
        userId: java.util.UUID?,
        query: String,
        ip: String?,
        result: SearchResponse
    ) {
        val tenant = tenants.findByCode(tenantCode) ?: return
        val sources = result.items.map { it.source }.distinct().toTypedArray()
        jdbc.update({ connection ->
            val ps = connection.prepareStatement(
                """
                INSERT INTO search_audit (tenant_id, user_id, query, ip, took_ms, sources)
                VALUES (?, ?, ?, ?, ?, ?)
                """.trimIndent()
            )
            ps.setObject(1, tenant.id)
            ps.setObject(2, userId)
            ps.setString(3, query.take(500))
            ps.setString(4, ip)
            ps.setInt(5, result.tookMs.toInt())
            ps.setArray(6, connection.createArrayOf("text", sources))
            ps
        })
    }
}
