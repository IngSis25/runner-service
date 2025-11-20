package analyzer

import org.amshove.kluent.shouldNotBeNull
import org.junit.jupiter.api.Test

class AnalyzerServiceEdgeCasesTest {
    private val service = AnalyzerService()

    @Test
    fun `analyze with empty config map should work`() {
        val code = "let x = 1; println(x)"
        val request =
            AnalyzeRequest(
                version = "1.0",
                source = code,
                config = emptyMap(),
            )

        val result = service.analyze(request)

        result.shouldNotBeNull()
    }

    @Test
    fun `analyze with nested config maps should work`() {
        val code = "let x = 1; println(x)"
        val config =
            mapOf<String, Any?>(
                "PrintUseCheck" to
                    mapOf(
                        "printlnCheckEnabled" to true,
                        "warnOnPrint" to false,
                    ),
            )
        val request =
            AnalyzeRequest(
                version = "1.0",
                source = code,
                config = config,
            )

        val result = service.analyze(request)

        result.shouldNotBeNull()
    }
}
