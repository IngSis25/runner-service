package runner.config.consumers

data class IncomingTestMessage(
    val testId: Long,
    val snippetId: Long,
    val userId: String,
    val version: String,
    val jwtToken: String,
    val inputs: List<String>,
    val outputs: List<String>,
)
