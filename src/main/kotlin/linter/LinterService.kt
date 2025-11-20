package linter

import analyzer.JsonConverter
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.google.gson.Gson
import kotlinx.serialization.json.JsonObject
import org.springframework.stereotype.Service
import runner.Runner
import types.Rule
import java.io.StringReader
import com.google.gson.JsonObject as GsonJsonObject

@Service
class LinterService {
    private val gson = Gson()
    private val objectMapper = ObjectMapper()

    fun analyze(
        version: String,
        code: String,
        rules: Map<String, Any?>?,
    ): List<String> {
        val reader = StringReader(code)
        val runner = Runner(version, reader)

        // Convertir el config a com.google.gson.JsonObject (que es lo que espera el Runner)
        val rulesJson: GsonJsonObject =
            if (rules.isNullOrEmpty()) {
                GsonJsonObject()
            } else {
                convertMapToGsonJsonObject(rules)
            }

        val result = runner.analyze(rulesJson)
        return result.warnings
    }

    fun analyze(
        version: String,
        code: String,
        rules: JsonObject,
    ): List<String> {
        val reader = StringReader(code)
        val runner = Runner(version, reader)

        // Convertir kotlinx.serialization.json.JsonObject a com.google.gson.JsonObject
        val gsonRules = convertKotlinxToGsonJsonObject(rules)
        val result = runner.analyze(gsonRules)
        return result.warnings
    }

    fun convertActiveRulesToJsonObject(rules: List<Rule>): JsonObject {
        val activeRuleMap: Map<String, JsonNode> =
            rules
                .filter { it.isActive }
                .associate { rule ->
                    val key = rule.name
                    val value: JsonNode =
                        when (rule.value) {
                            null ->
                                when (key) {
                                    "PrintUseCheck" -> {
                                        val printUseNode = objectMapper.createObjectNode()
                                        printUseNode.put("printlnCheckEnabled", true)
                                        printUseNode
                                    }
                                    "ReadInputCheck" -> {
                                        val readInputNode = objectMapper.createObjectNode()
                                        readInputNode.put("readInputCheckEnabled", true)
                                        readInputNode
                                    }
                                    else -> JsonNodeFactory.instance.nullNode()
                                }
                            is String ->
                                when (key) {
                                    "NamingFormatCheck" -> {
                                        val namingPatternNode = objectMapper.createObjectNode()
                                        namingPatternNode.put("namingPatternName", rule.value)
                                        namingPatternNode
                                    }
                                    else -> JsonNodeFactory.instance.textNode(rule.value)
                                }
                            else -> objectMapper.valueToTree(rule.value)
                        }
                    key to value
                }
        return JsonConverter.convertToKotlinxJson(activeRuleMap)
    }

    private fun convertMapToGsonJsonObject(map: Map<String, Any?>): GsonJsonObject {
        // Convertir el Map a JSON string y luego a Gson JsonObject
        val jsonString = gson.toJson(map)
        return gson.fromJson(jsonString, GsonJsonObject::class.java)
    }

    private fun convertKotlinxToGsonJsonObject(jsonObject: JsonObject): GsonJsonObject {
        // Convertir kotlinx.serialization.json.JsonObject a com.google.gson.JsonObject
        val jsonString = jsonObject.toString()
        return gson.fromJson(jsonString, GsonJsonObject::class.java)
    }
}
