package app.infrastructure.engine

import app.domain.model.Snippet
import app.domain.model.ValidationError
import app.domain.model.ValidationReport
import app.domain.ports.RunnerEngine
import org.springframework.stereotype.Component

// === Tu PrintScript (ajustá paquetes si en tu ZIP son otros) ===
import factory.LexerFactoryRegistry
import main.kotlin.lexer.Lexer
import main.kotlin.lexer.LexicalException
import main.kotlin.lexer.Token
import main.kotlin.parser.DefaultParser
import main.kotlin.parser.ConfiguredRules
import rules.RuleMatcher

// resolver de posición: dado un mensaje, devuelve (line,col) o null
private typealias PosResolver = (String?) -> Pair<Int, Int>?

private data class ParserWithResolver(
    val parser: DefaultParser,
    val resolver: PosResolver
)

@Component
class PrintScriptEngine : RunnerEngine {

    override fun validate(snippet: Snippet): ValidationReport {
        val version = normalizeVersion(snippet.version)

        // 1) LEXING
        val lexer: Lexer = LexerFactoryRegistry.getFactory(version).create()
        val tokens: List<Token> = try {
            lexer.tokenize(snippet.content)
        } catch (e: LexicalException) {
            val charIndex: Int? = extractFirstInt(e.message)
            val pos: Pair<Int, Int> = charIndex?.let { indexToLineCol(snippet.content, it) } ?: (0 to 0)
            return ValidationReport(
                errors = listOf(
                    ValidationError(
                        rule = "Lexical",
                        message = e.message ?: "Lexical error",
                        line = pos.first,
                        column = pos.second
                    )
                )
            )
        }

        // 2) PARSER + reglas por versión
        val pw: ParserWithResolver = buildParser(version, tokens)

        return try {
            // Si tu parse devuelve una lista de nodos o Unit, no importa: si no lanza, es válido
            pw.parser.parse(tokens)
            ValidationReport(errors = emptyList())
        } catch (e: IllegalArgumentException) {
            // Parser suele tirar IllegalArgumentException con "pos token"
            val pos: Pair<Int, Int> = pw.resolver(e.message) ?: (0 to 0)
            ValidationReport(
                errors = listOf(
                    ValidationError(
                        rule = "Syntax",
                        message = e.message ?: "Syntax error",
                        line = pos.first,
                        column = pos.second
                    )
                )
            )
        }
    }


    private fun buildParser(version: String, tokens: List<Token>): ParserWithResolver {
        return when (version) {
            "1.0" -> {
                val matcher = RuleMatcher(ConfiguredRules.V1)
                val parser = DefaultParser(matcher)
                val resolver: PosResolver = { msg: String? ->
                    val idx: Int? = extractFirstInt(msg)
                    idx?.let { tokenPosToLineCol(tokens, it) }
                }
                ParserWithResolver(parser, resolver)
            }
            "1.1" -> {
                // algunas implementaciones generan reglas 1.1 a partir de un parser “base”
                val tmp = DefaultParser(RuleMatcher(emptyList()))
                val rulesV11 = ConfiguredRules.createV11Rules(tmp)
                val parser = DefaultParser(RuleMatcher(rulesV11))
                val resolver: PosResolver = { msg: String? ->
                    val idx: Int? = extractFirstInt(msg)
                    idx?.let { tokenPosToLineCol(tokens, it) }
                }
                ParserWithResolver(parser, resolver)
            }
            else -> error("Versión no soportada: $version")
        }
    }

    private fun normalizeVersion(v: String?): String =
        when (v?.trim()) {
            null, "", "1", "1.0" -> "1.0"
            "1.1" -> "1.1"
            else -> v.trim()
        }


    private fun extractFirstInt(text: String?): Int? {
        if (text == null) return null
        val m = Regex("(\\d+)").find(text) ?: return null
        return m.groupValues.getOrNull(1)?.toIntOrNull()
    }


    private fun indexToLineCol(source: String, index: Int): Pair<Int, Int> {
        var line = 1
        var col = 1
        var i = 0
        while (i < source.length && i < index) {
            if (source[i] == '\n') {
                line += 1
                col = 1
            } else {
                col += 1
            }
            i += 1
        }
        return line to col
    }


    private fun tokenPosToLineCol(tokens: List<Token>, pos: Int): Pair<Int, Int> {
        if (pos in tokens.indices) {
            val t = tokens[pos]
            return t.line to t.column
        }
        return tokens.lastOrNull()?.let { it.line to it.column } ?: (0 to 0)
    }
}
