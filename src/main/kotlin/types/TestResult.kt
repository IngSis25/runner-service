package types

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.Instant

data class TestResult(
    @JsonProperty("testId") val testId: Long,
    @JsonProperty("status") val status: TestStatus,
    @JsonProperty("errors") val errors: List<String>,
    @JsonProperty("executedAt") val executedAt: Instant,
)

enum class TestStatus {
    PASSED,
    FAILED,
}
