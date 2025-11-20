package tests

import io.mockk.every
import io.mockk.mockk
import org.amshove.kluent.shouldBeEmpty
import org.junit.jupiter.api.Test
import utils.AssetService

class TestResultServiceEdgeCasesTest {
    private val assetService = mockk<AssetService>(relaxed = true)
    private val service = TestResultService(assetService)

    @Test
    fun `getResults should handle blank payload`() {
        val snippetId = 1L

        every { assetService.get("test-results", snippetId) } returns ""

        val result = service.getResults(snippetId)

        result.shouldBeEmpty()
    }

    @Test
    fun `getResults should handle whitespace only payload`() {
        val snippetId = 1L

        every { assetService.get("test-results", snippetId) } returns "   "

        val result = service.getResults(snippetId)

        result.shouldBeEmpty()
    }

    @Test
    fun `getResults should handle invalid JSON gracefully`() {
        val snippetId = 1L

        every { assetService.get("test-results", snippetId) } returns "invalid json"

        val result = service.getResults(snippetId)

        result.shouldBeEmpty()
    }
}
