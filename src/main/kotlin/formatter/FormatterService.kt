package formatter

import com.google.gson.Gson
import factory.LexerFactoryRegistry
import main.kotlin.parser.ConfiguredRules
import main.kotlin.parser.DefaultParser
import org.example.ast.ASTNode
import org.example.formatter.Formatter
import org.example.formatter.config.FormatterConfig
import org.springframework.stereotype.Service
import rules.ParserRule
import rules.RuleMatcher
import java.io.File

@Service
class FormatterService {
    private val gson = Gson()

    fun format(req: FormatRequest): String {
        val version = normalizeVersion(req.version)
        val lexer = LexerFactoryRegistry.getFactory(req.version).create()

        val parser =
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
                else -> error("Unsupported PrintScript version: ${req.version}")
            }
        val configFile = materializeConfigFile(req)
        val onlyBreaks = req.onlyLineBreakAfterStatement ?: false
        if (onlyBreaks) {
            return Formatter.formatSource(req.source, configFile)
        }
        val ast: List<ASTNode> = parser.parse(lexer.tokenize(req.source)).toList()
        return Formatter.formatMultiple(ast, configFile)
    }

    private fun materializeConfigFile(req: FormatRequest): File {
        val cfg =
            if (req.config.isNullOrEmpty()) {
                FormatterConfig()
            } else {
                gson.fromJson(gson.toJson(req.config), FormatterConfig::class.java)
            }
        val tmp =
            kotlin.io.path
                .createTempFile("formatter-config-", ".json")
                .toFile()
        tmp.writeText(gson.toJson(cfg), Charsets.UTF_8)
        return tmp
    }

    private fun normalizeVersion(v: String): String {
        val x = v.trim().lowercase().removePrefix("v")
        return when (x) {
            "1", "1.0" -> "1.0"
            "1.1" -> "1.1"
            else -> x
        }
    }
}
