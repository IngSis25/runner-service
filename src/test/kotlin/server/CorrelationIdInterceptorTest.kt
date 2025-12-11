package server

import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.slf4j.MDC
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpRequest
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.http.client.ClientHttpResponse
import runner.server.CorrelationIdFilter
import runner.server.CorrelationIdInterceptor

class CorrelationIdInterceptorTest {
    @Test
    fun `should add correlation ID from MDC to request headers`() {
        val interceptor = CorrelationIdInterceptor()
        val request = mock<HttpRequest>()
        val headers = HttpHeaders()
        val execution = mock<ClientHttpRequestExecution>()
        val response = mock<ClientHttpResponse>()

        whenever(request.headers).thenReturn(headers)
        whenever(execution.execute(any(), any())).thenReturn(response)

        MDC.put(CorrelationIdFilter.CORRELATION_ID_KEY, "test-correlation-id")

        try {
            interceptor.intercept(request, ByteArray(0), execution)

            verify(execution).execute(any(), any())
            assert(headers["X-Request-ID"]?.contains("test-correlation-id") == true)
            assert(headers[CorrelationIdFilter.CORRELATION_ID_HEADER]?.contains("test-correlation-id") == true)
        } finally {
            MDC.remove(CorrelationIdFilter.CORRELATION_ID_KEY)
        }
    }

    @Test
    fun `should generate new correlation ID if MDC is empty`() {
        val interceptor = CorrelationIdInterceptor()
        val request = mock<HttpRequest>()
        val headers = HttpHeaders()
        val execution = mock<ClientHttpRequestExecution>()
        val response = mock<ClientHttpResponse>()

        whenever(request.headers).thenReturn(headers)
        whenever(execution.execute(any(), any())).thenReturn(response)

        // Ensure MDC is empty
        MDC.remove(CorrelationIdFilter.CORRELATION_ID_KEY)

        interceptor.intercept(request, ByteArray(0), execution)

        verify(execution).execute(any(), any())
        assert(headers["X-Request-ID"]?.isNotEmpty() == true)
        assert(headers[CorrelationIdFilter.CORRELATION_ID_HEADER]?.isNotEmpty() == true)
    }
}
