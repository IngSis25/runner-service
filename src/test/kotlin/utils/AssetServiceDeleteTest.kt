package utils

import io.mockk.justRun
import io.mockk.mockk
import org.amshove.kluent.shouldNotBeNull
import org.junit.jupiter.api.Test
import org.springframework.web.client.RestTemplate
import runner.utils.AssetService

class AssetServiceDeleteTest {
    private val restTemplate = mockk<RestTemplate>(relaxed = true)
    private val service = AssetService(restTemplate)

    @Test
    fun `delete should call asset service`() {
        val bucket = "snippets"
        val id = 1L

        justRun {
            restTemplate.delete(any<String>())
        }

        val result = service.delete(bucket, id)

        result.shouldNotBeNull()
    }
}
