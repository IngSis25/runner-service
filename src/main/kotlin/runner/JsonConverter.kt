package analyzer

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.NullNode
import com.fasterxml.jackson.databind.node.NumericNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.databind.node.TextNode
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

object JsonConverter {
    fun convertToKotlinxJson(jsonNode: Map<String, JsonNode>): JsonObject {
        val jsonMap =
            jsonNode.mapValues { entry ->
                val element =
                    when (val value = entry.value) {
                        is ObjectNode -> Json.parseToJsonElement(ObjectMapper().writeValueAsString(value))
                        is ArrayNode -> Json.parseToJsonElement(ObjectMapper().writeValueAsString(value))
                        is NumericNode -> JsonPrimitive(value.numberValue())
                        is NullNode -> JsonNull
                        is TextNode -> JsonPrimitive(value.textValue())
                        else -> Json.parseToJsonElement(value.toString())
                    }
                element
            }
        return JsonObject(jsonMap)
    }

    fun convertToJacksonJson(jsonObject: JsonObject): Map<String, JsonNode> {
        val jsonMap =
            jsonObject.mapValues { entry ->
                com.fasterxml.jackson.databind
                    .ObjectMapper()
                    .readTree(entry.value.toString())
            }
        return jsonMap
    }
}
