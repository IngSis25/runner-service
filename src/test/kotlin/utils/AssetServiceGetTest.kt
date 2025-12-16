package utils

import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.springframework.web.client.RestTemplate
import runner.utils.ASSETSERVICE_URL
import runner.utils.AssetService

class AssetServiceGetTest {
    private val restTemplate = mockk<RestTemplate>(relaxed = true)
    private val service = AssetService(restTemplate)

    @Test
    fun `get should return response when found`() {
        val bucket = "snippets"
        val id = 1L
        val expectedContent = "let x = 1;"

        every {
            restTemplate.getForObject("$ASSETSERVICE_URL/$bucket/$id", String::class.java)
        } returns expectedContent

        val result = service.get(bucket, id)

        assert(result == expectedContent)
    }

    @Test
    fun `get should return default message when response is null`() {
        val bucket = "snippets"
        val id = 1L

        every {
            restTemplate.getForObject("$ASSETSERVICE_URL/$bucket/$id", String::class.java)
        } returns null

        val result = service.get(bucket, id)

        assert(result == "Search in $bucket not found")
    }

    @Test
    fun `get should handle different directories`() {
        val bucket = "test-results"
        val id = 2L
        val expectedContent = "test results"

        every {
            restTemplate.getForObject("$ASSETSERVICE_URL/$bucket/$id", String::class.java)
        } returns expectedContent

        val result = service.get(bucket, id)

        assert(result == expectedContent)
    }
}
