package config

import org.junit.jupiter.api.Test
import runner.config.TestDefinition
import runner.config.TestExecutionMessage

class TestExecutionMessageTest {
    @Test
    fun `should create TestExecutionMessage with all fields`() {
        val tests =
            listOf(
                TestDefinition(1L, listOf("input1"), listOf("output1")),
                TestDefinition(2L, listOf("input2"), listOf("output2")),
            )
        val message =
            TestExecutionMessage(
                snippetId = 1L,
                userId = "user123",
                version = "1.0",
                jwtToken = "jwt-token",
                tests = tests,
            )

        assert(message.snippetId == 1L)
        assert(message.userId == "user123")
        assert(message.version == "1.0")
        assert(message.jwtToken == "jwt-token")
        assert(message.tests.size == 2)
        assert(message.tests[0].id == 1L)
        assert(message.tests[1].id == 2L)
    }

    @Test
    fun `should create TestDefinition with all fields`() {
        val definition =
            TestDefinition(
                id = 1L,
                inputs = listOf("input1", "input2"),
                outputs = listOf("output1", "output2"),
            )

        assert(definition.id == 1L)
        assert(definition.inputs.size == 2)
        assert(definition.outputs.size == 2)
        assert(definition.inputs[0] == "input1")
        assert(definition.outputs[0] == "output1")
    }

    @Test
    fun `should create TestExecutionMessage with empty tests`() {
        val message =
            TestExecutionMessage(
                snippetId = 1L,
                userId = "user123",
                version = "1.0",
                jwtToken = "jwt-token",
                tests = emptyList(),
            )

        assert(message.tests.isEmpty())
    }
}
