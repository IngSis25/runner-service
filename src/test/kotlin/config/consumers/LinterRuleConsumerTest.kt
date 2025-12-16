package config.consumers

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import kotlinx.serialization.json.JsonObject
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doNothing
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.data.redis.connection.stream.ObjectRecord
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.test.context.ActiveProfiles
import runner.config.SnippetMessage
import runner.config.consumers.LinterRuleConsumer
import runner.linter.LinterService
import runner.snippet.SnippetService
import runner.types.Compliance
import runner.utils.AssetService

@ActiveProfiles("test")
class LinterRuleConsumerTest {
    private val redisTemplate = mock<ReactiveRedisTemplate<String, String>>()
    private val lintService = mock<LinterService>()
    private val assetService = mock<AssetService>()
    private val snippetService = mock<SnippetService>()
    private val consumer =
        LinterRuleConsumer(
            redisTemplate = redisTemplate,
            streamKey = "testStreamKey",
            groupId = "testGroupId",
            lintService = lintService,
            assetService = assetService,
            snippetService = snippetService,
        )

    @Test
    fun `should lint code successfully`() {
        val message = SnippetMessage(1L, 1L, "1.0", "jwt-token")
        val record = mock<ObjectRecord<String, String>>()
        whenever(record.value).thenReturn(jacksonObjectMapper().writeValueAsString(message))
        whenever(assetService.get("lint-rules", message.userId)).thenReturn("""[{"id":"1","name":"PrintUseCheck","isActive":true,"value":null}]""")
        whenever(lintService.convertActiveRulesToJsonObject(any())).thenReturn(JsonObject(emptyMap()))
        whenever(assetService.get("snippets", message.snippetId)).thenReturn("some code")
        whenever(lintService.analyze(message.version, "some code", JsonObject(emptyMap()))).thenReturn(emptyList())
        doNothing().whenever(snippetService).updateStatus(any(), any(), eq(Compliance.FAILED))
        whenever(assetService.put(any(), any(), any())).thenReturn("OK")

        consumer.onMessage(record)
    }

    @Test
    fun `should handle linting exception`() {
        val message = SnippetMessage(1L, 1L, "1.0", "jwt-token")
        val record = mock<ObjectRecord<String, String>>()
        whenever(record.value).thenReturn(jacksonObjectMapper().writeValueAsString(message))
        whenever(assetService.get("snippets", message.snippetId)).thenThrow(RuntimeException("Error getting snippet"))
        doNothing().whenever(snippetService).updateStatus(any(), any(), eq(Compliance.FAILED))

        consumer.onMessage(record)
    }

    @Test
    fun `should handle linting with warnings`() {
        val message = SnippetMessage(1L, 1L, "1.0", "jwt-token")
        val record = mock<ObjectRecord<String, String>>()
        whenever(record.value).thenReturn(jacksonObjectMapper().writeValueAsString(message))
        whenever(assetService.get("lint-rules", message.userId)).thenReturn("""[{"id":"1","name":"PrintUseCheck","isActive":true,"value":null}]""")
        whenever(lintService.convertActiveRulesToJsonObject(any())).thenReturn(JsonObject(emptyMap()))
        whenever(assetService.get("snippets", message.snippetId)).thenReturn("some code")
        whenever(lintService.analyze(message.version, "some code", JsonObject(emptyMap()))).thenReturn(listOf("Warning 1", "Warning 2"))
        doNothing().whenever(snippetService).updateStatus(any(), any(), eq(Compliance.FAILED))
        whenever(assetService.put(any(), any(), any())).thenReturn("OK")

        consumer.onMessage(record)
    }
}
