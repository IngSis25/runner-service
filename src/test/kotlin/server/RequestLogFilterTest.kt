package server

import jakarta.servlet.FilterChain
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.slf4j.MDC
import runner.server.CorrelationIdFilter
import runner.server.RequestLogFilter

class RequestLogFilterTest {
    @Test
    fun `should log request with correlation ID`() {
        val filter = RequestLogFilter()
        val request = mock<HttpServletRequest>()
        val response = mock<HttpServletResponse>()
        val chain = mock<FilterChain>()

        whenever(request.requestURI).thenReturn("/test")
        whenever(request.method).thenReturn("GET")
        whenever(response.status).thenReturn(200)

        MDC.put(CorrelationIdFilter.CORRELATION_ID_KEY, "test-id")

        try {
            filter.doFilter(request, response, chain)

            verify(chain).doFilter(request, response)
            verify(response).status
        } finally {
            MDC.remove(CorrelationIdFilter.CORRELATION_ID_KEY)
        }
    }

    @Test
    fun `should log request without correlation ID`() {
        val filter = RequestLogFilter()
        val request = mock<HttpServletRequest>()
        val response = mock<HttpServletResponse>()
        val chain = mock<FilterChain>()

        whenever(request.requestURI).thenReturn("/test")
        whenever(request.method).thenReturn("POST")
        whenever(response.status).thenReturn(404)

        // Ensure MDC is empty
        MDC.remove(CorrelationIdFilter.CORRELATION_ID_KEY)

        filter.doFilter(request, response, chain)

        verify(chain).doFilter(request, response)
        verify(response).status
    }

    @Test
    fun `should handle non-http request and response`() {
        val filter = RequestLogFilter()
        val request = mock<ServletRequest>()
        val response = mock<ServletResponse>()
        val chain = mock<FilterChain>()

        filter.doFilter(request, response, chain)

        verify(chain).doFilter(request, response)
    }
}
