package app.application

import app.domain.model.ValidationReport
import app.domain.model.VersionMismatchException
import app.domain.ports.RunnerEngine
import app.domain.ports.SnippetGateway
import org.springframework.stereotype.Service

@Service
class ValidateSnippetUseCase(
    private val snippets: SnippetGateway,
    private val engine: RunnerEngine
) {
    suspend fun execute(snippetId: String, requestedVersion: String?): ValidationReport {
        val sn = snippets.getSnippet(snippetId, requestedVersion)
        if (requestedVersion != null && sn.version != requestedVersion) {
            throw VersionMismatchException(requestedVersion, sn.version)
        }
        return engine.validate(sn)
    }
}
