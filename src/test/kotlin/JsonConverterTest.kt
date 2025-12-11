package analyzer

import analyzer.JsonConverter
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.NullNode
import com.fasterxml.jackson.databind.node.NumericNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.databind.node.TextNode
import kotlinx.serialization.json.JsonObject
import org.amshove.kluent.shouldNotBeNull
import org.junit.jupiter.api.Test

class JsonConverterTest {
    private val objectMapper = ObjectMapper()

    @Test
    fun `should convert TextNode to JsonPrimitive`() {
        val textNode: TextNode = objectMapper.valueToTree("test")
        val map = mapOf("key" to textNode)

        val result = JsonConverter.convertToKotlinxJson(map)

        result.shouldNotBeNull()
        assert(result["key"]?.toString() == "\"test\"")
    }

    @Test
    fun `should convert NumericNode to JsonPrimitive`() {
        val numericNode: NumericNode = objectMapper.valueToTree(42)
        val map = mapOf("key" to numericNode)

        val result = JsonConverter.convertToKotlinxJson(map)

        result.shouldNotBeNull()
        assert(result["key"]?.toString() == "42")
    }

    @Test
    fun `should convert NullNode to JsonNull`() {
        val nullNode = objectMapper.nullNode() as NullNode
        val map = mapOf("key" to nullNode)

        val result = JsonConverter.convertToKotlinxJson(map)

        result.shouldNotBeNull()
        assert(result["key"]?.toString() == "null")
    }

    @Test
    fun `should convert ObjectNode to JsonObject`() {
        val objectNode: ObjectNode =
            objectMapper.createObjectNode().apply {
                put("nested", "value")
            }
        val map = mapOf("key" to objectNode)

        val result = JsonConverter.convertToKotlinxJson(map)

        result.shouldNotBeNull()
        assert(result["key"] != null)
    }

    @Test
    fun `should convert ArrayNode to JsonArray`() {
        val arrayNode: ArrayNode =
            objectMapper.createArrayNode().apply {
                add("item1")
                add("item2")
            }
        val map = mapOf("key" to arrayNode)

        val result = JsonConverter.convertToKotlinxJson(map)

        result.shouldNotBeNull()
        assert(result["key"] != null)
    }

    @Test
    fun `should convert JsonObject to Jackson JsonNode map`() {
        val jsonObject = JsonObject(mapOf("key" to kotlinx.serialization.json.JsonPrimitive("value")))

        val result = JsonConverter.convertToJacksonJson(jsonObject)

        result.shouldNotBeNull()
        assert(result.containsKey("key"))
        assert(result["key"]?.asText() == "value")
    }

    @Test
    fun `should convert complex JsonObject to Jackson JsonNode map`() {
        val jsonObject =
            JsonObject(
                mapOf(
                    "string" to kotlinx.serialization.json.JsonPrimitive("test"),
                    "number" to kotlinx.serialization.json.JsonPrimitive(123),
                ),
            )

        val result = JsonConverter.convertToJacksonJson(jsonObject)

        result.shouldNotBeNull()
        assert(result.containsKey("string"))
        assert(result.containsKey("number"))
        assert(result["string"]?.asText() == "test")
        assert(result["number"]?.asInt() == 123)
    }
}
