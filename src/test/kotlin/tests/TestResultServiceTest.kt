package tests

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.kotlinModule
import io.mockk.every
import io.mockk.mockk
import org.amshove.kluent.shouldBeEmpty
import org.amshove.kluent.shouldEqual
import org.junit.jupiter.api.Test
import types.TestResult
import types.TestStatus
import utils.AssetService
import java.time.Instant

class TestResultServiceTest {
    private val assetService = mockk<AssetService>(relaxed = true)
    private val service = TestResultService(assetService)

    @Test
    fun `getResults should return empty list when no results exist`() {
        val snippetId = 1L

        every { assetService.get("test-results", snippetId) } throws Exception("Not found")

        val result = service.getResults(snippetId)

        result.shouldBeEmpty()
    }

    @Test
    fun `getResults should return test results when they exist`() {
        val snippetId = 1L
        val now = Instant.parse("2024-01-01T00:00:00Z")
        // Configurar ObjectMapper con JavaTimeModule para serializar Instant
        val objectMapper =
            ObjectMapper()
                .registerModule(JavaTimeModule())
                .registerModule(kotlinModule())
        val testResults =
            listOf(
                TestResult(
                    testId = 1L,
                    status = TestStatus.PASSED,
                    errors = emptyList(),
                    executedAt = now,
                ),
                TestResult(
                    testId = 2L,
                    status = TestStatus.FAILED,
                    errors = listOf("Error 1", "Error 2"),
                    executedAt = now,
                ),
            )
        val serialized = objectMapper.writeValueAsString(testResults)

        every { assetService.get("test-results", snippetId) } returns serialized

        val result = service.getResults(snippetId)

        result.size shouldEqual 2
        result[0].status shouldEqual TestStatus.PASSED
        result[1].status shouldEqual TestStatus.FAILED
        result[1].errors.size shouldEqual 2
    }
}
