package runner.interpreter

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import runner.types.TestStatus

/**
 * Internal controller for synchronous test execution.
 * This endpoint is intended to be called by snippet-service only.
 * It executes a test directly without using Redis Streams or asset-service.
 */
@RestController
@RequestMapping("/internal/tests")
class InternalTestController(
    private val interpreterService: InterpreterService,
) {
    /**
     * Execute a test synchronously with code provided directly.
     *
     * @param request the test execution request containing code, version, inputs, and expected outputs
     * @return TestExecutionResultDTO with status (PASSED/FAILED) and error messages
     */
    @PostMapping("/run")
    fun runTest(
        @RequestBody request: TestExecutionRequestDTO,
    ): ResponseEntity<TestExecutionResultDTO> {
        val result =
            interpreterService.executeTest(
                version = request.version,
                code = request.code,
                inputs = request.inputs,
                expectedOutputs = request.expectedOutputs,
            )

        val dto =
            TestExecutionResultDTO(
                status = if (result.status == TestStatus.PASSED) "PASSED" else "FAILED",
                errors = result.errors,
            )

        return ResponseEntity.ok(dto)
    }
}
