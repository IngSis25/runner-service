package runner.config

data class TestExecutionMessage(
    val snippetId: Long,
    val userId: Long,
    val version: String,
    val jwtToken: String,
    val tests: List<TestDefinition>,
)

data class TestDefinition(
    val id: Long,
    val inputs: List<String>,
    val outputs: List<String>,
)
