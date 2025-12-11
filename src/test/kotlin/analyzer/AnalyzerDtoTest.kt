package analyzer

import org.junit.jupiter.api.Test
import runner.analyzer.AnalyzeRequest

class AnalyzerDtoTest {
    @Test
    fun `should create AnalyzeRequest with all fields`() {
        val config =
            mapOf<String, Any?>(
                "PrintUseCheck" to mapOf("printlnCheckEnabled" to true),
            )
        val request =
            AnalyzeRequest(
                version = "1.0",
                source = "let x = 1;",
                config = config,
            )

        assert(request.version == "1.0")
        assert(request.source == "let x = 1;")
        assert(request.config != null)
        assert(request.config?.containsKey("PrintUseCheck") == true)
    }

    @Test
    fun `should create AnalyzeRequest with null config`() {
        val request =
            AnalyzeRequest(
                version = "1.0",
                source = "let x = 1;",
                config = null,
            )

        assert(request.config == null)
    }

    @Test
    fun `should create AnalyzeRequest with version one one`() {
        val request =
            AnalyzeRequest(
                version = "1.1",
                source = "let x = 1;",
                config = null,
            )

        assert(request.version == "1.1")
    }

    @Test
    fun `should create AnalyzeRequest with empty source`() {
        val request =
            AnalyzeRequest(
                version = "1.0",
                source = "",
                config = null,
            )

        assert(request.source.isEmpty())
    }
}
