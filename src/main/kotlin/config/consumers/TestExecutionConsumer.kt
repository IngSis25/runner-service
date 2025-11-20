package config.consumers

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import config.TestExecutionMessage
import interpreter.InterpreterService
import org.austral.ingsis.redis.RedisStreamConsumer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.data.redis.connection.stream.ObjectRecord
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.data.redis.stream.StreamReceiver
import org.springframework.stereotype.Service
import snippet.SnippetService
import types.Compliance
import types.TestResult
import types.TestStatus
import utils.AssetService
import java.time.Duration
import java.time.Instant

@Service
@Profile("!test")
class TestExecutionConsumer
    @Autowired
    constructor(
        redisTemplate: ReactiveRedisTemplate<String, String>,
        @Value("\${stream.test.key}") streamKey: String,
        @Value("\${groups.test}") groupId: String,
        private val interpreterService: InterpreterService,
        private val assetService: AssetService,
        private val snippetService: SnippetService,
    ) : RedisStreamConsumer<String>(streamKey, groupId, redisTemplate) {
        private val objectMapper = jacksonObjectMapper()

        override fun options(): StreamReceiver.StreamReceiverOptions<String, ObjectRecord<String, String>> =
            StreamReceiver.StreamReceiverOptions
                .builder()
                .pollTimeout(Duration.ofMillis(10000))
                .targetType(String::class.java)
                .build()

        public override fun onMessage(record: ObjectRecord<String, String>) {
            val message: TestExecutionMessage = objectMapper.readValue(record.value)
            try {
                val testResults = executeTests(message)
                persistResults(message.snippetId, testResults)
                val success = testResults.none { it.status == TestStatus.FAILED }
                snippetService.updateStatus(
                    message.jwtToken,
                    message.snippetId,
                    if (success) Compliance.SUCCESS else Compliance.FAILED,
                )
            } catch (ex: Exception) {
                snippetService.updateStatus(message.jwtToken, message.snippetId, Compliance.FAILED)
            }
        }

        private fun executeTests(message: TestExecutionMessage): List<TestResult> {
            val executedAt = Instant.now()
            return message.tests.map { definition ->
                val errors =
                    interpreterService.test(
                        version = message.version,
                        snippetId = message.snippetId,
                        inputs = definition.inputs,
                        expectedOutputs = definition.outputs,
                    )
                TestResult(
                    testId = definition.id,
                    status = if (errors.isEmpty()) TestStatus.PASSED else TestStatus.FAILED,
                    errors = errors,
                    executedAt = executedAt,
                )
            }
        }

        private fun persistResults(
            snippetId: Long,
            results: List<TestResult>,
        ) {
            val serialized = objectMapper.writeValueAsString(results)
            assetService.put("test-results", snippetId, serialized)
        }
    }
