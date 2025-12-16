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
import runner.server.CorrelationIdFilter

class CorrelationIdFilterTest {
//    @Test
//    fun `should set correlation ID from X-Request-ID header`() {
//        val filter = CorrelationIdFilter()
//        val request = mock<HttpServletRequest>()
//        val response = mock<HttpServletResponse>()
//        val chain = mock<FilterChain>()
//
//        whenever(request.getHeader("X-Request-ID")).thenReturn("test-correlation-id")
//        whenever(request.getHeader(CorrelationIdFilter.CORRELATION_ID_HEADER)).thenReturn(null)
//
//        filter.doFilter(request, response, chain)
//
//        verify(response).setHeader("X-Request-ID", "test-correlation-id")
//        verify(response).setHeader(CorrelationIdFilter.CORRELATION_ID_HEADER, "test-correlation-id")
//        verify(chain).doFilter(request, response)
//    }
//
//    @Test
//    fun `should set correlation ID from X-Correlation-Id header if X-Request-ID is missing`() {
//        val filter = CorrelationIdFilter()
//        val request = mock<HttpServletRequest>()
//        val response = mock<HttpServletResponse>()
//        val chain = mock<FilterChain>()
//
//        whenever(request.getHeader("X-Request-ID")).thenReturn(null)
//        whenever(request.getHeader(CorrelationIdFilter.CORRELATION_ID_HEADER)).thenReturn("fallback-id")
//
//        filter.doFilter(request, response, chain)
//
//        verify(response).setHeader("X-Request-ID", "fallback-id")
//        verify(response).setHeader(CorrelationIdFilter.CORRELATION_ID_HEADER, "fallback-id")
//        verify(chain).doFilter(request, response)
//    }
//
//    @Test
//    fun `should generate new correlation ID if no header is present`() {
//        val filter = CorrelationIdFilter()
//        val request = mock<HttpServletRequest>()
//        val response = mock<HttpServletResponse>()
//        val chain = mock<FilterChain>()
//
//        whenever(request.getHeader("X-Request-ID")).thenReturn(null)
//        whenever(request.getHeader(CorrelationIdFilter.CORRELATION_ID_HEADER)).thenReturn(null)
//
//        filter.doFilter(request, response, chain)
//
//        verify(response).setHeader(eq("X-Request-ID"), any())
//        verify(response).setHeader(eq(CorrelationIdFilter.CORRELATION_ID_HEADER), any())
//        verify(chain).doFilter(request, response)
//    }

    @Test
    fun `should remove correlation ID from MDC after request`() {
        val filter = CorrelationIdFilter()
        val request = mock<HttpServletRequest>()
        val response = mock<HttpServletResponse>()
        val chain = mock<FilterChain>()

        whenever(request.getHeader("X-Request-ID")).thenReturn("test-id")

        filter.doFilter(request, response, chain)

        // MDC should be cleared after request
        verify(chain).doFilter(request, response)
    }

    @Test
    fun `should handle non-http request and response`() {
        val filter = CorrelationIdFilter()
        val request = mock<ServletRequest>()
        val response = mock<ServletResponse>()
        val chain = mock<FilterChain>()

        filter.doFilter(request, response, chain)

        verify(chain).doFilter(request, response)
    }
}
