package tests

import org.junit.jupiter.api.Test
import runner.tests.RunTestDefinition
import runner.tests.RunTestsRequest

class RunTestsRequestTest {
    @Test
    fun `should create RunTestsRequest with all fields`() {
        val tests =
            listOf(
                RunTestDefinition(1L, listOf("input1"), listOf("output1")),
            )
        val request =
            RunTestsRequest(
                version = "1.0",
                snippetId = 1L,
                tests = tests,
            )

        assert(request.version == "1.0")
        assert(request.snippetId == 1L)
        assert(request.tests.size == 1)
    }

    @Test
    fun `should create RunTestsRequest with version one one`() {
        val request =
            RunTestsRequest(
                version = "1.1",
                snippetId = 2L,
                tests = emptyList(),
            )

        assert(request.version == "1.1")
        assert(request.snippetId == 2L)
    }

    @Test
    fun `should create RunTestDefinition with all fields`() {
        val definition =
            RunTestDefinition(
                id = 1L,
                inputs = listOf("input1", "input2"),
                outputs = listOf("output1", "output2"),
            )

        assert(definition.id == 1L)
        assert(definition.inputs.size == 2)
        assert(definition.outputs.size == 2)
    }
}
