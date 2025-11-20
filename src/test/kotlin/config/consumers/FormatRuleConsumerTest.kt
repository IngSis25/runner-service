package config.consumers

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import config.SnippetMessage
import formatter.FormatterService
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.data.redis.connection.stream.ObjectRecord
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.test.context.ActiveProfiles
import utils.AssetService

@ActiveProfiles("test")
class FormatRuleConsumerTest {
    private val redisTemplate = mock<ReactiveRedisTemplate<String, String>>()
    private val formatService = mock<FormatterService>()
    private val assetService = mock<AssetService>()
    private val consumer =
        FormatRuleConsumer(
            redisTemplate = redisTemplate,
            streamKey = "testStreamKey",
            groupId = "testGroupId",
            formatService = formatService,
            assetService = assetService,
        )

    @Test
    fun `should format code successfully`() {
        val message = SnippetMessage(1L, 1L, "1.0", "jwt-token")
        val record = mock<ObjectRecord<String, String>>()
        whenever(record.value).thenReturn(jacksonObjectMapper().writeValueAsString(message))
        whenever(assetService.get("format-rules", message.userId)).thenReturn("""[{"id":"1","name":"IndentSize","isActive":true,"value":"2"}]""")
        whenever(formatService.getActiveAdaptedRules(any())).thenReturn("""{"indent_size":"2"}""")
        whenever(assetService.get("snippets", message.snippetId)).thenReturn("some code")
        whenever(formatService.format(message.version, "some code", """{"indent_size":"2"}""")).thenReturn("formatted code")
        whenever(assetService.put(any(), any(), any())).thenReturn("OK")

        consumer.onMessage(record)
    }

    @Test
    fun `should handle formatting exception`() {
        val message = SnippetMessage(1L, 1L, "1.0", "jwt-token")
        val record = mock<ObjectRecord<String, String>>()
        whenever(record.value).thenReturn(jacksonObjectMapper().writeValueAsString(message))
        whenever(assetService.get("snippets", message.snippetId)).thenThrow(RuntimeException("Error getting snippet"))

        consumer.onMessage(record)
    }
}
