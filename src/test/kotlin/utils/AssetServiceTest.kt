package utils

import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import org.amshove.kluent.shouldNotBeNull
import org.junit.jupiter.api.Test
import org.springframework.web.client.RestTemplate

class AssetServiceTest {
    private val restTemplate = mockk<RestTemplate>(relaxed = true)
    private val service = AssetService(restTemplate)

    @Test
    fun `get should return content from asset service`() {
        val bucket = "snippets"
        val id = 1L
        val expectedContent = "let x = 1; println(x)"

        every {
            restTemplate.getForObject("${ASSETSERVICE_URL}/$bucket/$id", String::class.java)
        } returns expectedContent

        val result = service.get(bucket, id)

        result.shouldNotBeNull()
    }

    @Test
    fun `put should call asset service`() {
        val bucket = "snippets"
        val id = 1L
        val content = "let x = 1; println(x)"

        justRun {
            restTemplate.put(any<String>(), any<String>(), any())
        }

        val result = service.put(bucket, id, content)

        result.shouldNotBeNull()
    }
}
