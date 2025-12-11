package analyzer

import org.junit.jupiter.api.Test
import runner.analyzer.AnalyzeResponse
import runner.analyzer.DiagnosticDTO

class AnalyzerDtoMoreTests {
    @Test
    fun `should create DiagnosticDTO with all fields`() {
        val diagnostic =
            DiagnosticDTO(
                code = "CODE001",
                message = "Test message",
                severity = "WARNING",
                line = 1,
                column = 5,
                suggestions = listOf("Suggestion 1", "Suggestion 2"),
            )

        assert(diagnostic.code == "CODE001")
        assert(diagnostic.message == "Test message")
        assert(diagnostic.severity == "WARNING")
        assert(diagnostic.line == 1)
        assert(diagnostic.column == 5)
        assert(diagnostic.suggestions.size == 2)
    }

    @Test
    fun `should create DiagnosticDTO with empty suggestions`() {
        val diagnostic =
            DiagnosticDTO(
                code = "CODE002",
                message = "Test message",
                severity = "ERROR",
                line = 2,
                column = 10,
            )

        assert(diagnostic.suggestions.isEmpty())
    }

    @Test
    fun `should create AnalyzeResponse with ok true`() {
        val diagnostics =
            listOf(
                DiagnosticDTO("CODE001", "Message", "WARNING", 1, 1),
            )
        val response =
            AnalyzeResponse(
                ok = true,
                diagnostics = diagnostics,
            )

        assert(response.ok == true)
        assert(response.diagnostics.size == 1)
    }

    @Test
    fun `should create AnalyzeResponse with ok false`() {
        val diagnostics =
            listOf(
                DiagnosticDTO("CODE001", "Message", "ERROR", 1, 1),
            )
        val response =
            AnalyzeResponse(
                ok = false,
                diagnostics = diagnostics,
            )

        assert(response.ok == false)
        assert(response.diagnostics.size == 1)
    }

    @Test
    fun `should create AnalyzeResponse with empty diagnostics`() {
        val response =
            AnalyzeResponse(
                ok = true,
                diagnostics = emptyList(),
            )

        assert(response.diagnostics.isEmpty())
    }
}
