package config.consumers

import org.junit.jupiter.api.Test
import runner.config.consumers.IncomingTestMessage

class IncomingTestMessageTest {
    @Test
    fun `should create IncomingTestMessage with all fields`() {
        val message =
            IncomingTestMessage(
                testId = 1L,
                snippetId = 2L,
                userId = "auth0|123",
                version = "1.0",
                jwtToken = "jwt-token",
                inputs = listOf("input1", "input2"),
                outputs = listOf("output1", "output2"),
            )

        assert(message.testId == 1L)
        assert(message.snippetId == 2L)
        assert(message.userId == "auth0|123")
        assert(message.version == "1.0")
        assert(message.jwtToken == "jwt-token")
        assert(message.inputs.size == 2)
        assert(message.outputs.size == 2)
    }

    @Test
    fun `should create IncomingTestMessage with empty inputs and outputs`() {
        val message =
            IncomingTestMessage(
                testId = 1L,
                snippetId = 2L,
                userId = "auth0|123",
                version = "1.0",
                jwtToken = "jwt-token",
                inputs = emptyList(),
                outputs = emptyList(),
            )

        assert(message.inputs.isEmpty())
        assert(message.outputs.isEmpty())
    }
}
