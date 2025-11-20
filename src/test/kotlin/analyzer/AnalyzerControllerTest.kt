package analyzer

import io.mockk.every
import io.mockk.mockk
import org.amshove.kluent.shouldNotBeNull
import org.junit.jupiter.api.Test

class AnalyzerControllerTest {
    private val analyzerService = mockk<AnalyzerService>(relaxed = true)
    private val controller = AnalyzerController(analyzerService)

    @Test
    fun `analyze should call service and return result`() {
        val request =
            AnalyzeRequest(
                version = "1.0",
                source = "let x = 1; println(x)",
                config = null,
            )

        every { analyzerService.analyze(request) } returns emptyList()

        val result = controller.analyze(request)

        result.shouldNotBeNull()
    }
}
