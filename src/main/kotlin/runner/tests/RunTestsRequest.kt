package runner.tests

data class RunTestsRequest(
    val version: String,
    val snippetId: Long,
    val tests: List<RunTestDefinition>,
)

data class RunTestDefinition(
    val id: Long,
    val inputs: List<String>,
    val outputs: List<String>,
)
