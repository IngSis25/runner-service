package runner.interpreter

data class TestExecutionResultDTO(
    val status: String, // "PASSED" or "FAILED"
    val errors: List<String> = emptyList(),
)
