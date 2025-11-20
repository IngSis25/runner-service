package snippet

import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.web.client.RestTemplate
import types.Compliance

class SnippetServiceTest {
    private val restTemplate = mockk<RestTemplate>(relaxed = true)
    private val service = SnippetService(restTemplate)

    @Test
    fun `updateStatus should call snippet service`() {
        val jwtToken = "Bearer token"
        val snippetId = 1L
        val status = Compliance.SUCCESS

        every {
            restTemplate.exchange(
                any<String>(),
                any<HttpMethod>(),
                any<HttpEntity<*>>(),
                any<Class<*>>(),
            )
        } returns mockk(relaxed = true)

        service.updateStatus(jwtToken, snippetId, status)
    }
}
