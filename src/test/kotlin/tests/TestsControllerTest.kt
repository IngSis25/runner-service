package tests

import interpreter.InterpreterService
import io.mockk.every
import io.mockk.mockk
import org.amshove.kluent.shouldNotBeNull
import org.junit.jupiter.api.Test

class TestsControllerTest {
    private val interpreterService = mockk<InterpreterService>(relaxed = true)
    private val testResultService = mockk<TestResultService>(relaxed = true)
    private val controller = TestsController(interpreterService, testResultService)

    @Test
    fun `getResults should call service and return result`() {
        val snippetId = 1L

        every { testResultService.getResults(snippetId) } returns emptyList()

        val result = controller.getResults(snippetId)

        result.shouldNotBeNull()
    }

    @Test
    fun `runTests should call service and return result`() {
        val request =
            RunTestsRequest(
                version = "1.0",
                snippetId = 1L,
                tests = emptyList(),
            )

        val result = controller.runTests(request)

        result.shouldNotBeNull()
    }
}
