package config.consumers

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.data.redis.connection.stream.ObjectRecord
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.test.context.ActiveProfiles
import runner.config.consumers.TestExecutionConsumer
import runner.interpreter.InterpreterService
import runner.snippet.SnippetService
import runner.utils.AssetService

@ActiveProfiles("test")
class TestExecutionConsumerTest {
    private val redisTemplate = mock<ReactiveRedisTemplate<String, String>>()
    private val interpreterService = mock<InterpreterService>()
    private val assetService = mock<AssetService>()
    private val snippetService = mock<SnippetService>()
    private val consumer =
        TestExecutionConsumer(
            redisTemplate = redisTemplate,
            streamKey = "testStreamKey",
            groupId = "testGroupId",
            interpreterService = interpreterService,
            assetService = assetService,
            snippetService = snippetService,
        )

    @Test
    fun `should execute tests successfully`() {
        val testMessage =
            runner.config.consumers.IncomingTestMessage(
                testId = 1L,
                snippetId = 1L,
                userId = "auth0|123",
                version = "1.0",
                jwtToken = "jwt-token",
                inputs = emptyList(),
                outputs = listOf("Hello"),
            )
        val record = mock<ObjectRecord<String, String>>()
        whenever(record.value).thenReturn(jacksonObjectMapper().writeValueAsString(testMessage))
        whenever(interpreterService.test("1.0", 1L, emptyList(), listOf("Hello"))).thenReturn(emptyList())
        whenever(assetService.put(eq("test-results"), eq(1L), any())).thenReturn("OK")
        consumer.onMessage(record)
    }

    @Test
    fun `should handle test failures`() {
        val testMessage =
            runner.config.consumers.IncomingTestMessage(
                testId = 1L,
                snippetId = 1L,
                userId = "auth0|123",
                version = "1.0",
                jwtToken = "jwt-token",
                inputs = emptyList(),
                outputs = listOf("Hello"),
            )
        val record = mock<ObjectRecord<String, String>>()
        whenever(record.value).thenReturn(jacksonObjectMapper().writeValueAsString(testMessage))
        whenever(interpreterService.test("1.0", 1L, emptyList(), listOf("Hello"))).thenReturn(listOf("Error"))
        whenever(assetService.put(eq("test-results"), eq(1L), any())).thenReturn("OK")

        consumer.onMessage(record)
    }

    @Test
    fun `should handle exceptions gracefully`() {
        val testMessage =
            runner.config.consumers.IncomingTestMessage(
                testId = 1L,
                snippetId = 1L,
                userId = "auth0|123",
                version = "1.0",
                jwtToken = "jwt-token",
                inputs = emptyList(),
                outputs = listOf("Hello"),
            )
        val record = mock<ObjectRecord<String, String>>()
        whenever(record.value).thenReturn(jacksonObjectMapper().writeValueAsString(testMessage))
        whenever(interpreterService.test(any(), any(), any(), any())).thenThrow(RuntimeException("Test error"))
        consumer.onMessage(record)
    }

    @Test
    fun `should handle invalid JSON gracefully`() {
        val record = mock<ObjectRecord<String, String>>()
        whenever(record.value).thenReturn("invalid json")
        consumer.onMessage(record)
    }

    @Test
    fun `should handle exception when saving results fails`() {
        val testMessage =
            runner.config.consumers.IncomingTestMessage(
                testId = 1L,
                snippetId = 1L,
                userId = "auth0|123",
                version = "1.0",
                jwtToken = "jwt-token",
                inputs = emptyList(),
                outputs = listOf("Hello"),
            )
        val record = mock<ObjectRecord<String, String>>()
        whenever(record.value).thenReturn(jacksonObjectMapper().writeValueAsString(testMessage))
        whenever(interpreterService.test("1.0", 1L, emptyList(), listOf("Hello"))).thenReturn(emptyList())
        whenever(assetService.put(eq("test-results"), eq(1L), any())).thenThrow(RuntimeException("Save error"))
        try {
            consumer.onMessage(record)
        } catch (e: Exception) {
            // Expected to throw
            assert(e.message?.contains("Save error") == true)
        }
    }
}
