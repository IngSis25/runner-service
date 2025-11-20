package analyzer

import org.amshove.kluent.shouldNotBeNull
import org.junit.jupiter.api.Test

class AnalyzerServiceTest {
    private val service = AnalyzerService()

    @Test
    fun `analyze should return diagnostics for code`() {
        val code = "let x = 1; println(x)"
        val version = "1.0"
        val config =
            mapOf<String, Any?>(
                "PrintUseCheck" to mapOf("printlnCheckEnabled" to true),
            )
        val request =
            AnalyzeRequest(
                version = version,
                source = code,
                config = config,
            )

        val result = service.analyze(request)

        // Puede retornar warnings o errores
        result.shouldNotBeNull()
    }

    @Test
    fun `analyze should handle code without config`() {
        val code = "let x = 1; println(x)"
        val version = "1.0"
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
    fun `analyze should handle empty code`() {
        val code = ""
        val version = "1.0"
        val request =
            AnalyzeRequest(
                version = version,
                source = code,
                config = null,
            )

        val result = service.analyze(request)

        result.shouldNotBeNull()
    }
}
