package analyzer

import com.google.gson.Gson
import factory.LexerFactoryRegistry
import main.kotlin.analyzer.AnalyzerConfig
import main.kotlin.analyzer.DefaultAnalyzer
import main.kotlin.analyzer.Diagnostic
import main.kotlin.analyzer.DiagnosticSeverity
import main.kotlin.lexer.Lexer
import main.kotlin.parser.ConfiguredRules
import main.kotlin.parser.DefaultParser
import org.example.ast.ASTNode
import org.springframework.stereotype.Service
import rules.ParserRule
import rules.RuleMatcher

@Service
class AnalyzerService {
    private val gson = Gson()

    fun analyze(req: AnalyzeRequest): List<DiagnosticDTO> {
        val version = normalizeVersion(req.version)

        // 1) Lexer + Parser para la versión (DefaultParser espera RuleMatcher)
        val lexer: Lexer = LexerFactoryRegistry.getFactory(version).create()
        val parser: DefaultParser =
            when (version) {
                "1.0" -> {
                    val rules: List<ParserRule> = ConfiguredRules.V1
                    DefaultParser(RuleMatcher(rules))
                }
                "1.1" -> {
                    val bootstrap = DefaultParser(RuleMatcher(emptyList<ParserRule>()))
                    val rulesV11: List<ParserRule> = ConfiguredRules.createV11Rules(bootstrap)
                    DefaultParser(RuleMatcher(rulesV11))
                }
                else -> error("Unsupported PrintScript version: $version")
            }

        // 2) Parsear programa
        val tokens = lexer.tokenize(req.source)
        val program: List<ASTNode> = parser.parse(tokens).toList()

        // 3) Config (map → AnalyzerConfig) con defaults tuyos
        val config: AnalyzerConfig =
            if (req.config.isNullOrEmpty()) {
                AnalyzerConfig()
            } else {
                gson.fromJson(gson.toJson(req.config), AnalyzerConfig::class.java)
            }

        // 4) Ejecutar analyzer y mapear a DTO
        val result = DefaultAnalyzer().analyze(program, config) // devuelve AnalysisResult con diagnostics
        return result.diagnostics.map { it.toDTO() }
    }

    private fun Diagnostic.toDTO(): DiagnosticDTO =
        DiagnosticDTO(
            code = this.code,
            message = this.message,
            severity =
                when (this.severity) {
                    DiagnosticSeverity.ERROR -> "ERROR"
                    DiagnosticSeverity.WARNING -> "WARNING"
                    DiagnosticSeverity.INFO -> "INFO"
                },
            line = this.position.line,
            column = this.position.column,
            suggestions = this.suggestions,
        )

    private fun normalizeVersion(v: String): String {
        val x = v.trim().lowercase().removePrefix("v")
        return when (x) {
            "1", "1.0" -> "1.0"
            "1.1" -> "1.1"
            else -> x
        }
    }
}
