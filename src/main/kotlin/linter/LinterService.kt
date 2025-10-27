package linter

import kotlinx.serialization.json.JsonObject
import main.kotlin.analyzer.AnalyzerConfig
import org.springframework.stereotype.Service
import runner.Runner
import java.io.StringReader

@Service
class LinterService {
    fun analyze(version: String, code: String, rules: AnalyzerConfig): List<String> {
        val runner = Runner(
            version = version,
            sourceCode = code
        )
        val result = runner.analyze(rules)
        return result.warnings
    }
}
