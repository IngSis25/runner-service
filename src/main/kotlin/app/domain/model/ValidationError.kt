package app.domain.model

data class ValidationError(
    val rule: String,
    val message: String,
    val line: Int,
    val column: Int
)
