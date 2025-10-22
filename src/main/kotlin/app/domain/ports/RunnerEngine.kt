package app.domain.ports

import app.domain.model.Snippet
import app.domain.model.ValidationReport

interface RunnerEngine {
    fun validate(snippet: Snippet): ValidationReport
}
