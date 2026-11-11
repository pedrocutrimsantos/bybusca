package br.bybusca.common

import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.util.UUID

data class ApiError(
    val code: String,
    val message: String,
    val traceId: String
)

class ApiException(
    val code: String,
    message: String,
    val status: HttpStatus = HttpStatus.BAD_REQUEST
) : RuntimeException(message)

@RestControllerAdvice
class GlobalExceptionHandler {
    @ExceptionHandler(ApiException::class)
    fun handleApi(ex: ApiException, request: HttpServletRequest): ResponseEntity<ApiError> {
        val traceId = request.getHeader("X-Trace-Id") ?: UUID.randomUUID().toString()
        return ResponseEntity.status(ex.status).body(ApiError(ex.code, ex.message ?: "", traceId))
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(ex: MethodArgumentNotValidException, request: HttpServletRequest): ResponseEntity<ApiError> {
        val traceId = request.getHeader("X-Trace-Id") ?: UUID.randomUUID().toString()
        val msg = ex.bindingResult.fieldErrors.firstOrNull()?.defaultMessage ?: "Dados inválidos"
        return ResponseEntity.badRequest().body(ApiError("VALIDATION_ERROR", msg, traceId))
    }

    @ExceptionHandler(Exception::class)
    fun handleGeneric(ex: Exception, request: HttpServletRequest): ResponseEntity<ApiError> {
        val traceId = request.getHeader("X-Trace-Id") ?: UUID.randomUUID().toString()
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiError("INTERNAL_ERROR", "Não foi possível concluir a solicitação", traceId))
    }
}
