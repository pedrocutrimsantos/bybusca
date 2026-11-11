package br.bybusca.auth

import br.bybusca.acl.SearchPrincipal
import br.bybusca.common.ApiException
import br.bybusca.config.ByBuscaProperties
import br.bybusca.domain.TenantRepository
import br.bybusca.domain.UserRepository
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.http.HttpStatus
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.nio.charset.StandardCharsets
import java.time.Instant
import java.util.Date
import java.util.UUID
import javax.crypto.SecretKey

data class LoginRequest(val username: String, val password: String)
data class LoginResponse(
    val accessToken: String,
    val expiresIn: Long,
    val user: UserView
)
data class UserView(
    val id: UUID,
    val username: String,
    val displayName: String,
    val roles: List<String>
)

@Service
class AuthService(
    private val users: UserRepository,
    private val tenants: TenantRepository,
    private val passwordEncoder: PasswordEncoder,
    private val props: ByBuscaProperties
) {
    private fun key(): SecretKey =
        Keys.hmacShaKeyFor(props.jwt.secret.toByteArray(StandardCharsets.UTF_8).let {
            if (it.size >= 32) it else it + ByteArray(32 - it.size)
        })

    fun login(tenantCode: String, req: LoginRequest): LoginResponse {
        val tenant = tenants.findByCode(tenantCode)
            ?: throw ApiException("TENANT_NOT_FOUND", "Escola não encontrada", HttpStatus.NOT_FOUND)
        val user = users.findByTenantIdAndUsername(tenant.id, req.username)
            ?: throw ApiException("INVALID_CREDENTIALS", "Usuário ou senha inválidos", HttpStatus.UNAUTHORIZED)
        if (!user.active || !passwordEncoder.matches(req.password, user.passwordHash)) {
            throw ApiException("INVALID_CREDENTIALS", "Usuário ou senha inválidos", HttpStatus.UNAUTHORIZED)
        }
        val roles = users.findRoleCodes(user.id)
        val expiresIn = props.jwt.expirationMinutes * 60
        val token = Jwts.builder()
            .subject(user.id.toString())
            .claim("tenant", tenant.code)
            .claim("roles", roles)
            .claim("classIds", user.classIds.toList())
            .claim("subjectIds", user.subjectIds.toList())
            .claim("name", user.displayName)
            .expiration(Date.from(Instant.now().plusSeconds(expiresIn)))
            .signWith(key())
            .compact()
        return LoginResponse(token, expiresIn, UserView(user.id, user.username, user.displayName, roles))
    }

    fun parsePrincipal(token: String?, tenantHeader: String?): SearchPrincipal {
        val tenantCode = tenantHeader?.takeIf { it.isNotBlank() } ?: props.defaultTenant
        if (token.isNullOrBlank()) {
            return SearchPrincipal(tenantCode = tenantCode, roles = setOf("GUEST"))
        }
        return try {
            val claims = Jwts.parser().verifyWith(key()).build()
                .parseSignedClaims(token.removePrefix("Bearer ").trim())
                .payload
            @Suppress("UNCHECKED_CAST")
            val roles = (claims["roles"] as? List<String>)?.toSet() ?: setOf("GUEST")
            @Suppress("UNCHECKED_CAST")
            val classIds = (claims["classIds"] as? List<String>)?.toSet() ?: emptySet()
            @Suppress("UNCHECKED_CAST")
            val subjectIds = (claims["subjectIds"] as? List<String>)?.toSet() ?: emptySet()
            SearchPrincipal(
                userId = UUID.fromString(claims.subject),
                tenantCode = (claims["tenant"] as? String) ?: tenantCode,
                roles = roles,
                classIds = classIds,
                subjectIds = subjectIds
            )
        } catch (_: Exception) {
            SearchPrincipal(tenantCode = tenantCode, roles = setOf("GUEST"))
        }
    }
}
