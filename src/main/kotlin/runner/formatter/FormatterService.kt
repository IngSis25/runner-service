package runner.formatter

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.google.gson.Gson
import org.springframework.stereotype.Service
import runner.Runner
import runner.types.Rule
import java.io.StringReader

@Service
class FormatterService {
    private val gson = Gson()
    private val objectMapper = jacksonObjectMapper()

    fun format(req: FormatRequest): String {
        val version = normalizeVersion(req.version)
        val reader = StringReader(req.source)
        val runner = Runner(version, reader)

        // Convertir el config a JSON string (que es lo que espera el Runner)
        val rulesJson: String =
            if (req.config.isNullOrEmpty()) {
                "{}"
            } else {
                gson.toJson(req.config)
            }

        val result = runner.format(rulesJson, version)
        return result.formattedCode
    }

    fun format(
        version: String,
        code: String,
        rules: String,
    ): String {
        val reader = StringReader(code)
        val runner = Runner(version, reader)
        val result = runner.format(rules, version)
        return result.formattedCode
    }

    fun getActiveAdaptedRules(formatRulesJson: String): String {
        val formatRules: List<Rule> = objectMapper.readValue(formatRulesJson, object : TypeReference<List<Rule>>() {})

        val rulesMap = mutableMapOf<String, Any?>()
        formatRules.forEach { rule ->
            if (rule.isActive) {
                val key = camelToSnakeCase(rule.name)
                rulesMap[key] = rule.value
            }
        }

        return objectMapper.writeValueAsString(rulesMap)
    }

    private fun camelToSnakeCase(camelCase: String): String =
        camelCase
            .replace(Regex("([a-z])([A-Z])"), "$1_$2")
            .lowercase()

    private fun normalizeVersion(v: String): String {
        val x = v.trim().lowercase().removePrefix("v")
        return when (x) {
            "1", "1.0" -> "1.0"
            "1.1" -> "1.1"
            else -> x
        }
    }
}
