package config.consumers

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doNothing
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.data.redis.connection.stream.ObjectRecord
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.test.context.ActiveProfiles
import runner.config.consumers.TestExecutionConsumer
import runner.interpreter.InterpreterService
import runner.snippet.SnippetService
import runner.types.Compliance
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
            mapOf(
                "testId" to 1L,
                "snippetId" to 1L,
                "userId" to "auth0|123",
                "version" to "1.0",
                "jwtToken" to "jwt-token",
                "inputs" to emptyList<String>(),
                "outputs" to listOf("Hello"),
            )
        val message =
            mapOf(
                "type" to "test",
                "data" to jacksonObjectMapper().writeValueAsString(testMessage),
            )
        val record = mock<ObjectRecord<String, String>>()
        whenever(record.value).thenReturn(jacksonObjectMapper().writeValueAsString(message))
        whenever(interpreterService.test("1.0", 1L, emptyList(), listOf("Hello"))).thenReturn(emptyList())
        whenever(assetService.put(eq("test-results"), eq(1L), any())).thenReturn("OK")
        doNothing().whenever(snippetService).updateStatus(any(), any(), eq(Compliance.FAILED))

        consumer.onMessage(record)
    }

    @Test
    fun `should handle test failures`() {
        val testMessage =
            mapOf(
                "testId" to 1L,
                "snippetId" to 1L,
                "userId" to "auth0|123",
                "version" to "1.0",
                "jwtToken" to "jwt-token",
                "inputs" to emptyList<String>(),
                "outputs" to listOf("Hello"),
            )
        val message =
            mapOf(
                "type" to "test",
                "data" to jacksonObjectMapper().writeValueAsString(testMessage),
            )
        val record = mock<ObjectRecord<String, String>>()
        whenever(record.value).thenReturn(jacksonObjectMapper().writeValueAsString(message))
        whenever(interpreterService.test("1.0", 1L, emptyList(), listOf("Hello"))).thenReturn(listOf("Error"))
        whenever(assetService.put(eq("test-results"), eq(1L), any())).thenReturn("OK")
        doNothing().whenever(snippetService).updateStatus(any(), any(), eq(Compliance.FAILED))

        consumer.onMessage(record)
    }

    @Test
    fun `should handle exceptions gracefully`() {
        val testMessage =
            mapOf(
                "testId" to 1L,
                "snippetId" to 1L,
                "userId" to "auth0|123",
                "version" to "1.0",
                "jwtToken" to "jwt-token",
                "inputs" to emptyList<String>(),
                "outputs" to listOf("Hello"),
            )
        val message =
            mapOf(
                "type" to "test",
                "data" to jacksonObjectMapper().writeValueAsString(testMessage),
            )
        val record = mock<ObjectRecord<String, String>>()
        whenever(record.value).thenReturn(jacksonObjectMapper().writeValueAsString(message))
        whenever(interpreterService.test(any(), any(), any(), any())).thenThrow(RuntimeException("Test error"))
        doNothing().whenever(snippetService).updateStatus(any(), any(), eq(Compliance.FAILED))

        consumer.onMessage(record)
    }
}
