package config.consumers

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import config.TestDefinition
import config.TestExecutionMessage
import interpreter.InterpreterService
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doNothing
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.data.redis.connection.stream.ObjectRecord
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.test.context.ActiveProfiles
import snippet.SnippetService
import types.Compliance
import utils.AssetService

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
        val message =
            TestExecutionMessage(
                snippetId = 1L,
                userId = 1L,
                version = "1.0",
                jwtToken = "jwt-token",
                tests =
                    listOf(
                        TestDefinition(id = 1L, inputs = emptyList(), outputs = listOf("Hello")),
                    ),
            )
        val record = mock<ObjectRecord<String, String>>()
        whenever(record.value).thenReturn(jacksonObjectMapper().writeValueAsString(message))
        whenever(interpreterService.test("1.0", 1L, emptyList(), listOf("Hello"))).thenReturn(emptyList())
        whenever(assetService.put(any(), any(), any())).thenReturn("OK")
        doNothing().whenever(snippetService).updateStatus(any(), any(), eq(Compliance.FAILED))

        consumer.onMessage(record)
    }

    @Test
    fun `should handle test failures`() {
        val message =
            TestExecutionMessage(
                snippetId = 1L,
                userId = 1L,
                version = "1.0",
                jwtToken = "jwt-token",
                tests =
                    listOf(
                        TestDefinition(id = 1L, inputs = emptyList(), outputs = listOf("Hello")),
                    ),
            )
        val record = mock<ObjectRecord<String, String>>()
        whenever(record.value).thenReturn(jacksonObjectMapper().writeValueAsString(message))
        whenever(interpreterService.test("1.0", 1L, emptyList(), listOf("Hello"))).thenReturn(listOf("Error"))
        whenever(assetService.put(any(), any(), any())).thenReturn("OK")
        doNothing().whenever(snippetService).updateStatus(any(), any(), eq(Compliance.FAILED))

        consumer.onMessage(record)
    }

    @Test
    fun `should handle exceptions gracefully`() {
        val message =
            TestExecutionMessage(
                snippetId = 1L,
                userId = 1L,
                version = "1.0",
                jwtToken = "jwt-token",
                tests =
                    listOf(
                        TestDefinition(id = 1L, inputs = emptyList(), outputs = listOf("Hello")),
                    ),
            )
        val record = mock<ObjectRecord<String, String>>()
        whenever(record.value).thenReturn(jacksonObjectMapper().writeValueAsString(message))
        whenever(interpreterService.test(any(), any(), any(), any())).thenThrow(RuntimeException("Test error"))
        doNothing().whenever(snippetService).updateStatus(any(), any(), eq(Compliance.FAILED))

        consumer.onMessage(record)
    }
}
