package analyzer

/** Request del analyzer */
data class AnalyzeRequest(
    val version: String,
    val source: String,
    val config: Map<String, Any?>? = null,
)

data class DiagnosticDTO(
    val code: String,
    val message: String,
    val severity: String,
    val line: Int,
    val column: Int,
    val suggestions: List<String> = emptyList(),
)

/** Response del analyzer */
data class AnalyzeResponse(
    val ok: Boolean,
    val diagnostics: List<DiagnosticDTO>,
)
