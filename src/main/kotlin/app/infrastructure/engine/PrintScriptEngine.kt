package app.infrastructure.engine

import app.domain.model.Snippet
import app.domain.model.ValidationReport
import app.domain.ports.RunnerEngine
import org.springframework.stereotype.Component
// Cambiarlo por el verdadero printscript
@Component
class PrintScriptEngine : RunnerEngine {
    override fun validate(snippet: Snippet): ValidationReport {
        // Stub: sin errores por ahora
        return ValidationReport(errors = emptyList())
    }
}
