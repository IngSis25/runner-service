package runner

sealed class RunnerResult {
    data class Analyze(
        val warnings: List<String>,
        val errors: List<String>,
    ) : RunnerResult()

    data class Format(
        val formattedCode: String,
        val errors: List<String>,
    ) : RunnerResult()

    data class Validate(
        val errors: List<String>,
    ) : RunnerResult()
}
