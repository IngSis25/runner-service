package config

import org.junit.jupiter.api.Test
import runner.config.SnippetMessage

class SnippetMessageTest {
    @Test
    fun `should create SnippetMessage with all fields`() {
        val message =
            SnippetMessage(
                snippetId = 1L,
                userId = 123L,
                version = "1.0",
                jwtToken = "jwt-token",
            )

        assert(message.snippetId == 1L)
        assert(message.userId == 123L)
        assert(message.version == "1.0")
        assert(message.jwtToken == "jwt-token")
    }

    @Test
    fun `should create SnippetMessage with version one one`() {
        val message =
            SnippetMessage(
                snippetId = 2L,
                userId = 456L,
                version = "1.1",
                jwtToken = "another-token",
            )

        assert(message.version == "1.1")
    }
}
