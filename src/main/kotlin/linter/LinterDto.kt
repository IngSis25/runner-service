package linter

data class LinterDto(
    val version: String,
    val code: String,
    val rules: Map<String, Any?>? = null,
)
