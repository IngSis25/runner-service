package runner.interpreter

data class TestExecutionRequestDTO(
    val version: String,
    val code: String,
    val inputs: List<String>? = null,
    val expectedOutputs: List<String>? = null,
)
