package app.domain.ports

import app.domain.model.Snippet

interface SnippetGateway {
    suspend fun getSnippet(snippetId: String, version: String? = null): Snippet
}
