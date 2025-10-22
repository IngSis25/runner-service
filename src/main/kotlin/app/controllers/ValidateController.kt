package app.controllers

import app.application.ValidateSnippetUseCase
import app.domain.model.Snippet
import app.domain.model.ValidationError
import app.domain.ports.RunnerEngine
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("/validate")
class ValidateController(
    private val useCase: ValidateSnippetUseCase,
    private val engine: RunnerEngine
) {
    data class Req(
        val snippetId: String? = null,
        val version: String? = null,
        val content: String? = null  // mandamos el codigo en el body
    )
    data class Res(val valid: Boolean, val errors: List<ValidationError>)

    @PostMapping(consumes = ["application/json"], produces = ["application/json"])
    suspend fun validate(@RequestBody req: Req): Res {
        // 1) si viene contenido inline, validamos directo
        if (!req.content.isNullOrBlank()) {
            val v = req.version ?: "1.0"
            val sn = Snippet(id = req.snippetId ?: "inline", version = v, content = req.content)
            val report = engine.validate(sn)
            return Res(valid = report.errors.isEmpty(), errors = report.errors)
        }

        // 2) si no hay content, esperamos snippetId
        require(!req.snippetId.isNullOrBlank()) { "snippetId or content is required" }
        val report = useCase.execute(req.snippetId, req.version)
        return Res(valid = report.errors.isEmpty(), errors = report.errors)
    }
}
