package config.consumers

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import kotlinx.serialization.json.JsonObject
import org.junit.jupiter.api.Test
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.data.redis.connection.stream.ObjectRecord
import org.springframework.data.redis.core.ReactiveRedisTemplate
import runner.config.consumers.RunnerStreamConsumer
import runner.formatter.FormatterService
import runner.linter.LinterService
import runner.snippet.SnippetService
import runner.types.Rule
import runner.utils.AssetService

class RunnerStreamConsumerTest {
    private val redisTemplate = mock<ReactiveRedisTemplate<String, String>>()
    private val formatService = mock<FormatterService>()
    private val lintService = mock<LinterService>()
    private val assetService = mock<AssetService>()
    private val snippetService = mock<SnippetService>()
    private val consumer =
        RunnerStreamConsumer(
            redisTemplate = redisTemplate,
            streamKey = "testStreamKey",
            groupId = "testGroupId",
            formatService = formatService,
            lintService = lintService,
            assetService = assetService,
            snippetService = snippetService,
        )

    @Test
    fun `should handle format message successfully`() {
        val messageMap =
            mapOf(
                "snippetId" to 1L,
                "userId" to "auth0|123",
                "version" to "1.0",
                "jwtToken" to "jwt-token",
            )
        val recordMap =
            mapOf(
                "type" to "format",
                "data" to jacksonObjectMapper().writeValueAsString(messageMap),
            )
        val record = mock<ObjectRecord<String, String>>()
        whenever(record.value).thenReturn(jacksonObjectMapper().writeValueAsString(recordMap))
        whenever(assetService.get(eq("format-rules"), anyOrNull())).thenReturn("""[{"id":"1","name":"IndentSize","isActive":true,"value":"2"}]""")
        whenever(formatService.getActiveAdaptedRules(anyOrNull())).thenReturn("""{"indent_size":"2"}""")
        whenever(assetService.get(eq("snippets"), eq(1L))).thenReturn("let x = 1;")
        whenever(formatService.format(eq("1.0"), eq("let x = 1;"), eq("""{"indent_size":"2"}"""))).thenReturn("let x = 1;")
        whenever(assetService.put(anyOrNull(), anyOrNull(), anyOrNull())).thenReturn("OK")

        consumer.onMessage(record)
    }

    @Test
    fun `should handle lint message successfully`() {
        val messageMap =
            mapOf(
                "snippetId" to 1L,
                "userId" to "auth0|123",
                "version" to "1.0",
                "jwtToken" to "jwt-token",
            )
        val recordMap =
            mapOf(
                "type" to "lint",
                "data" to jacksonObjectMapper().writeValueAsString(messageMap),
            )
        val record = mock<ObjectRecord<String, String>>()
        whenever(record.value).thenReturn(jacksonObjectMapper().writeValueAsString(recordMap))
        whenever(assetService.get(eq("lint-rules"), anyOrNull())).thenReturn("""[{"id":"1","name":"PrintUseCheck","isActive":true}]""")
        whenever(lintService.convertActiveRulesToJsonObject(anyOrNull<List<Rule>>())).thenReturn(JsonObject(emptyMap()))
        whenever(assetService.get(eq("snippets"), eq(1L))).thenReturn("let x = 1;")
        whenever(lintService.analyze(eq("1.0"), eq("let x = 1;"), anyOrNull<JsonObject>())).thenReturn(emptyList())
        whenever(assetService.put(anyOrNull(), anyOrNull(), anyOrNull())).thenReturn("OK")

        consumer.onMessage(record)
    }

    @Test
    fun `should handle snippet message as format`() {
        val messageMap =
            mapOf(
                "snippetId" to 1L,
                "userId" to "auth0|123",
                "version" to "1.0",
                "jwtToken" to "jwt-token",
            )
        val recordMap =
            mapOf(
                "type" to "snippet",
                "data" to jacksonObjectMapper().writeValueAsString(messageMap),
            )
        val record = mock<ObjectRecord<String, String>>()
        whenever(record.value).thenReturn(jacksonObjectMapper().writeValueAsString(recordMap))
        whenever(assetService.get(eq("format-rules"), anyOrNull())).thenReturn("""[{"id":"1","name":"IndentSize","isActive":true,"value":"2"}]""")
        whenever(formatService.getActiveAdaptedRules(anyOrNull())).thenReturn("""{"indent_size":"2"}""")
        whenever(assetService.get(eq("snippets"), eq(1L))).thenReturn("let x = 1;")
        whenever(formatService.format(eq("1.0"), eq("let x = 1;"), eq("""{"indent_size":"2"}"""))).thenReturn("let x = 1;")
        whenever(assetService.put(anyOrNull(), anyOrNull(), anyOrNull())).thenReturn("OK")

        consumer.onMessage(record)
    }

    @Test
    fun `should handle unknown message type`() {
        val recordMap =
            mapOf(
                "type" to "unknown",
                "data" to "{}",
            )
        val record = mock<ObjectRecord<String, String>>()
        whenever(record.value).thenReturn(jacksonObjectMapper().writeValueAsString(recordMap))

        consumer.onMessage(record)
    }

    @Test
    fun `should handle invalid JSON`() {
        val record = mock<ObjectRecord<String, String>>()
        whenever(record.value).thenReturn("invalid json")

        consumer.onMessage(record)
    }

    @Test
    fun `should handle format message with missing fields`() {
        val messageMap =
            mapOf<String, Any>(
                "snippetId" to 1L,
            )
        val recordMap =
            mapOf(
                "type" to "format",
                "data" to jacksonObjectMapper().writeValueAsString(messageMap),
            )
        val record = mock<ObjectRecord<String, String>>()
        whenever(record.value).thenReturn(jacksonObjectMapper().writeValueAsString(recordMap))

        consumer.onMessage(record)
    }

    @Test
    fun `should handle lint message with errors`() {
        val messageMap =
            mapOf(
                "snippetId" to 1L,
                "userId" to "auth0|123",
                "version" to "1.0",
                "jwtToken" to "jwt-token",
            )
        val recordMap =
            mapOf(
                "type" to "lint",
                "data" to jacksonObjectMapper().writeValueAsString(messageMap),
            )
        val record = mock<ObjectRecord<String, String>>()
        whenever(record.value).thenReturn(jacksonObjectMapper().writeValueAsString(recordMap))
        whenever(assetService.get(eq("lint-rules"), anyOrNull())).thenReturn("""[{"id":"1","name":"PrintUseCheck","isActive":true}]""")
        whenever(lintService.convertActiveRulesToJsonObject(anyOrNull<List<Rule>>())).thenReturn(JsonObject(emptyMap()))
        whenever(assetService.get(eq("snippets"), eq(1L))).thenReturn("let x = 1;")
        whenever(lintService.analyze(eq("1.0"), eq("let x = 1;"), anyOrNull<JsonObject>())).thenReturn(listOf("Warning: unused variable"))
        whenever(assetService.put(anyOrNull(), anyOrNull(), anyOrNull())).thenReturn("OK")

        consumer.onMessage(record)
    }
}
