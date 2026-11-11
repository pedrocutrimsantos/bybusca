package br.bybusca.auth

import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import org.springframework.web.bind.annotation.*

data class LoginBody(
    @field:NotBlank val username: String,
    @field:NotBlank val password: String
)

@RestController
@RequestMapping("/api/v1/auth")
class AuthController(private val authService: AuthService) {
    @PostMapping("/login")
    fun login(
        @RequestHeader(value = "X-Tenant", required = false) tenant: String?,
        @Valid @RequestBody body: LoginBody
    ): LoginResponse = authService.login(tenant ?: "demo-escola", LoginRequest(body.username, body.password))
}
