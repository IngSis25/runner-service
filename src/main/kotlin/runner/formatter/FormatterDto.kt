package runner.formatter

data class FormatRequest(
    val version: String,
    val source: String,
    val config: Map<String, Any?>? = null,
    val onlyLineBreakAfterStatement: Boolean? = null,
)

data class FormatResponse(
    val ok: Boolean,
    val formatted: String,
    val diagnostics: List<String> = emptyList(),
)
