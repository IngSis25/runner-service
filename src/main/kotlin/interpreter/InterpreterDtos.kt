package interpreter

data class RunRequest(
    val version: String,
    val source: String,
)

data class RunResponse(
    val ok: Boolean,
    val output: String,
    val diagnostics: List<String> = emptyList(),
)

data class RunResult(
    val output: String,
    val diagnostics: List<String>,
)
