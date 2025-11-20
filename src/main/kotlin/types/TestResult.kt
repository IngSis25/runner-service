package types
import java.time.Instant

data class TestResult(
    val testId: Long,
    val status: TestStatus,
    val errors: List<String>,
    val executedAt: Instant,
)

enum class TestStatus {
    PASSED,
    FAILED,
}
