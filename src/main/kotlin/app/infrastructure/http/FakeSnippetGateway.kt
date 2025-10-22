package app.infrastructure.http

import app.domain.model.Snippet
import app.domain.ports.SnippetGateway
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Profile("dev")
@Component
class FakeSnippetGateway : SnippetGateway {
    override suspend fun getSnippet(snippetId: String, version: String?): Snippet {
        val v = version ?: "1.0"
        return Snippet(
            id = snippetId,
            version = v,
            content = """println("Hola $snippetId v=$v")"""
        )
    }
}
