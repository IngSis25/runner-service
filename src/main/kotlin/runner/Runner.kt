package runner

import com.google.gson.JsonObject
import main.kotlin.analyzer.AnalyzerConfig
import main.kotlin.analyzer.AnalyzerFactory
import main.kotlin.analyzer.DefaultAnalyzer
import main.kotlin.analyzer.DiagnosticSeverity
import main.kotlin.lexer.Lexer
import main.kotlin.lexer.LexerFactory
import org.Parser
import org.ParserFactory
import org.example.astnode.ASTNode
import org.example.formatter.Formatter
import org.example.formatter.RulesFactory
import java.io.Reader

/**
 * Copia local del Runner de PrintScript con fix: mapea los diagnostics
 * (warnings/errors) en lugar de ignorarlos. Esto evita depender de una
 * versión parcheada publicada.
 */
class Runner(
    version: String,
    reader: Reader,
) {
    private val lexer: Lexer
    private val parser: Parser
    private val linter: DefaultAnalyzer
    private val formatter: Formatter
    private val runnerVersion: String = version

    init {
        when (version) {
            "1.0" -> {
                lexer = LexerFactory.createLexerV10(reader)
                parser = ParserFactory.createParserV10(lexer)
                linter = AnalyzerFactory().createAnalyzerV10(parser)
                formatter = Formatter(parser)
            }

            "1.1" -> {
                lexer = LexerFactory.createLexerV11(reader)
                parser = ParserFactory.createParserV11(lexer)
                linter = AnalyzerFactory().createAnalyzerV11(parser)
                formatter = Formatter(parser)
            }

            else -> throw IllegalArgumentException("Unsupported version: $version")
        }
    }

    fun analyze(jsonFile: JsonObject): RunnerResult.Analyze {
        val warningsList = mutableListOf<String>()
        val errorsList = mutableListOf<String>()

        // Collect AST nodes from the parser
        val nodes = mutableListOf<ASTNode>()
        while (parser.hasNext()) {
            try {
                nodes.add(parser.next())
            } catch (e: Exception) {
                errorsList.add(e.message ?: "Unknown parse error")
            }
        }

        val config = AnalyzerConfig(jsonConfig = jsonFile)
        val analysisResult = linter.analyze(nodes, config, runnerVersion)

        // Map diagnostics to warnings/errors explicitly
        analysisResult.diagnostics.forEach { diag ->
            when (diag.severity) {
                DiagnosticSeverity.WARNING -> warningsList.add("[${diag.code}] ${diag.message}")
                DiagnosticSeverity.ERROR -> errorsList.add("[${diag.code}] ${diag.message}")
                DiagnosticSeverity.INFO -> warningsList.add("[INFO][${diag.code}] ${diag.message}")
            }
        }

        return RunnerResult.Analyze(warningsList, errorsList)
    }

    fun format(
        json: String,
        version: String,
    ): RunnerResult.Format {
        val errorsList = mutableListOf<String>()

        val rules = RulesFactory().getRules(json, version)
        val formatterResult = formatter.format(rules)
        return RunnerResult.Format(formatterResult.code, errorsList)
    }

    fun validate(): RunnerResult.Validate {
        val errorsList = mutableListOf<String>()
        while (parser.hasNext()) {
            try {
                parser.next()
            } catch (e: Exception) {
                errorsList.add(e.message ?: "Unknown error")
            }
        }
        return RunnerResult.Validate(errorsList)
    }
}
