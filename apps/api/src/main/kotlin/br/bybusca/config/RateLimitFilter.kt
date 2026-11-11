package br.bybusca.config

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

/**
 * Rate limit simples por IP para busca/sugestão (RN15).
 * Janela fixa de 1 minuto; adequado à rede escolar no MVP.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 20)
class RateLimitFilter : OncePerRequestFilter() {
    private data class Window(val epochMinute: Long, val count: AtomicInteger)

    private val windows = ConcurrentHashMap<String, Window>()
    private val limitPerMinute = 120

    override fun shouldNotFilter(request: HttpServletRequest): Boolean {
        val path = request.requestURI
        return !(path.startsWith("/api/v1/search") || path.startsWith("/api/v1/suggest"))
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val ip = request.remoteAddr ?: "unknown"
        val minute = System.currentTimeMillis() / 60_000
        val window = windows.compute(ip) { _, current ->
            if (current == null || current.epochMinute != minute) Window(minute, AtomicInteger(0))
            else current
        }!!
        val count = window.count.incrementAndGet()
        if (count > limitPerMinute) {
            response.status = 429
            response.contentType = MediaType.APPLICATION_JSON_VALUE
            response.writer.write(
                """{"code":"RATE_LIMITED","message":"Muitas buscas em pouco tempo. Tente novamente em instantes.","traceId":"$minute-$ip"}"""
            )
            return
        }
        filterChain.doFilter(request, response)
    }
}
