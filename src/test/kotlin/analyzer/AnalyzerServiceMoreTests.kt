package analyzer

import org.amshove.kluent.shouldNotBeNull
import org.junit.jupiter.api.Test

class AnalyzerServiceMoreTests {
    private val service = AnalyzerService()

    @Test
    fun `analyze should handle version one one`() {
        val code = "let x = 1; println(x)"
        val version = "1.1"
        val request =
            AnalyzeRequest(
                version = version,
                source = code,
                config = null,
            )

        val result = service.analyze(request)

        result.shouldNotBeNull()
    }

    @Test
    fun `analyze should handle version normalization with v prefix`() {
        val code = "let x = 1; println(x)"
        val request =
            AnalyzeRequest(
                version = "v1.0",
                source = code,
                config = null,
            )

        val result = service.analyze(request)

        result.shouldNotBeNull()
    }

    @Test
    fun `analyze should handle complex config`() {
        val code = "let x = 1; println(x)"
        val config =
            mapOf<String, Any?>(
                "PrintUseCheck" to mapOf("printlnCheckEnabled" to true),
                "ReadInputCheck" to mapOf("readInputCheckEnabled" to true),
                "NamingFormatCheck" to mapOf("namingPatternName" to "camelCase"),
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
