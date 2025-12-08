package runner.server

import jakarta.servlet.Filter
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletException
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.MDC
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import java.io.IOException
import java.util.UUID

@Component
@Order(1)
class CorrelationIdFilter : Filter {
    @Throws(IOException::class, ServletException::class)
    override fun doFilter(
        request: ServletRequest,
        response: ServletResponse,
        chain: FilterChain,
    ) {
        if (request is HttpServletRequest && response is HttpServletResponse) {
            // Buscar primero X-Request-ID, luego X-Correlation-Id (para compatibilidad)
            var correlationId = request.getHeader("X-Request-ID")
                ?: request.getHeader(CORRELATION_ID_HEADER)
            
            if (correlationId == null) {
                correlationId = UUID.randomUUID().toString()
            }

            MDC.put(CORRELATION_ID_KEY, correlationId)

            // Establecer ambos headers para compatibilidad durante la transición
            response.setHeader("X-Request-ID", correlationId)
            response.setHeader(CORRELATION_ID_HEADER, correlationId)

            try {
                chain.doFilter(request, response)
            } finally {
                MDC.remove(CORRELATION_ID_KEY)
            }
        } else {
            chain.doFilter(request, response)
        }
    }

    companion object {
        const val CORRELATION_ID_KEY: String = "requestId"
        const val CORRELATION_ID_HEADER: String = "X-Request-ID"
    }
}
