package app.domain.model

data class ValidationReport(
    val errors: List<ValidationError>
)
