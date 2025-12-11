package runner

import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test
import runner.RunnerResult

class RunnerResultTest {
    @Test
    fun `should create Analyze result with warnings and errors`() {
        val warnings = listOf("warning1", "warning2")
        val errors = listOf("error1")

        val result = RunnerResult.Analyze(warnings, errors)

        result.warnings shouldBeEqualTo warnings
        result.errors shouldBeEqualTo errors
    }

    @Test
    fun `should create Format result with formatted code and errors`() {
        val formattedCode = "formatted code"
        val errors = emptyList<String>()

        val result = RunnerResult.Format(formattedCode, errors)

        result.formattedCode shouldBeEqualTo formattedCode
        result.errors shouldBeEqualTo errors
    }

    @Test
    fun `should create Format result with errors`() {
        val formattedCode = "formatted code"
        val errors = listOf("error1", "error2")

        val result = RunnerResult.Format(formattedCode, errors)

        result.formattedCode shouldBeEqualTo formattedCode
        result.errors shouldBeEqualTo errors
    }

    @Test
    fun `should create Validate result with errors`() {
        val errors = listOf("error1", "error2")

        val result = RunnerResult.Validate(errors)

        result.errors shouldBeEqualTo errors
    }

    @Test
    fun `should create Validate result with empty errors`() {
        val errors = emptyList<String>()

        val result = RunnerResult.Validate(errors)

        result.errors shouldBeEqualTo errors
    }
}
