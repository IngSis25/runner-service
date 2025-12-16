package runner.tests

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import runner.interpreter.InterpreterService
import runner.types.TestResult
import runner.types.TestStatus
import java.time.Instant

@RestController
@RequestMapping("/api/printscript/tests")
class TestsController(
    private val interpreterService: InterpreterService,
    private val testResultService: TestResultService,
) {
    @GetMapping("/{snippetId}")
    fun getResults(
        @PathVariable snippetId: Long,
    ): List<TestResult> = testResultService.getResults(snippetId)

    @PostMapping("/run")
    fun runTests(
        @RequestBody request: RunTestsRequest,
    ): List<TestResult> {
        val executedAt = Instant.now()
        return request.tests.map { testDefinition ->
            val errors =
                interpreterService.test(
                    version = request.version,
                    snippetId = request.snippetId,
                    inputs = testDefinition.inputs,
                    expectedOutputs = testDefinition.outputs,
                )
            TestResult(
                testId = testDefinition.id,
                status = if (errors.isEmpty()) TestStatus.PASSED else TestStatus.FAILED,
                errors = errors,
                executedAt = executedAt,
            )
        }
    }
}
