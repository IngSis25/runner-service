package runner.config.consumers

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.austral.ingsis.redis.RedisStreamConsumer
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.data.redis.connection.stream.ObjectRecord
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.data.redis.stream.StreamReceiver
import org.springframework.stereotype.Service
import runner.config.TestDefinition
import runner.config.TestExecutionMessage
import runner.interpreter.InterpreterService
import runner.snippet.SnippetService
import runner.types.Compliance
import runner.types.TestResult
import runner.types.TestStatus
import runner.utils.AssetService
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
        private val objectMapper =
            jacksonObjectMapper()
                .registerModule(JavaTimeModule())

        private val logger = LoggerFactory.getLogger(TestExecutionConsumer::class.java)

        override fun options(): StreamReceiver.StreamReceiverOptions<String, ObjectRecord<String, String>> =
            StreamReceiver.StreamReceiverOptions
                .builder()
                .pollTimeout(Duration.ofMillis(10000))
                .targetType(String::class.java)
                .build()

        public override fun onMessage(record: ObjectRecord<String, String>) {
            try {
                val rawJson = record.value
                println("TestExecutionConsumer recibió mensaje crudo: $rawJson")

                // 1) Parseo DIRECTO al mensaje que manda snippet-service
                val incoming = objectMapper.readValue(rawJson, IncomingTestMessage::class.java)

                // 2) Lo adapto a tu modelo interno con tests: List<TestDefinition>
                val testExecutionMessage =
                    TestExecutionMessage(
                        snippetId = incoming.snippetId,
                        userId = incoming.userId,
                        version = incoming.version,
                        jwtToken = incoming.jwtToken,
                        tests =
                            listOf(
                                TestDefinition(
                                    id = incoming.testId,
                                    inputs = incoming.inputs,
                                    outputs = incoming.outputs,
                                ),
                            ),
                    )

                println(
                    "TestExecutionConsumer procesando test para " +
                        "snippetId=${testExecutionMessage.snippetId}, " +
                        "testId=${testExecutionMessage.tests.first().id}",
                )

                val testResults = executeTests(testExecutionMessage)
                println(
                    "TestExecutionConsumer ejecutó ${testResults.size} test(s), " +
                        "resultados: ${testResults.map { "${it.testId}=${it.status}" }}",
                )

                persistResults(testExecutionMessage.snippetId, testResults)

                val success = testResults.none { it.status == TestStatus.FAILED }
                val finalStatus = if (success) Compliance.SUCCESS else Compliance.FAILED
                println("TestExecutionConsumer actualizando status del snippet ${testExecutionMessage.snippetId} a $finalStatus")

                snippetService.updateStatus(
                    testExecutionMessage.jwtToken,
                    testExecutionMessage.snippetId,
                    finalStatus,
                )

                println("TestExecutionConsumer completó procesamiento exitosamente para snippetId=${testExecutionMessage.snippetId}")
            } catch (ex: Exception) {
                println("Error procesando mensaje de test: ${ex.message}")
                ex.printStackTrace()

                // Intentar marcar el snippet como FAILED si podemos leer snippetId / jwtToken
                try {
                    val rawJson = record.value.toString()
                    val incoming = objectMapper.readValue(rawJson, IncomingTestMessage::class.java)
                    println("TestExecutionConsumer actualizando status a FAILED para snippetId=${incoming.snippetId} debido a error")
                    snippetService.updateStatus(incoming.jwtToken, incoming.snippetId, Compliance.FAILED)
                } catch (e: Exception) {
                    println("Error al actualizar status después de fallo: ${e.message}")
                }
            }
        }

        private fun executeTests(message: TestExecutionMessage): List<TestResult> {
            val executedAt = Instant.now()

            return message.tests.map { definition ->
                try {
                    val errors =
                        interpreterService.test(
                            version = message.version,
                            snippetId = message.snippetId,
                            inputs = definition.inputs,
                            expectedOutputs = definition.outputs,
                        )
                    logger.info(
                        """
                        Resultado test ${definition.id} para snippet ${message.snippetId}
                        • Inputs: ${definition.inputs}
                        • Expected outputs: ${definition.outputs}
                        • Errors devueltos por interpreterService.test(): $errors
                        """.trimIndent(),
                    )

                    TestResult(
                        testId = definition.id,
                        status = if (errors.isEmpty()) TestStatus.PASSED else TestStatus.FAILED,
                        errors = errors,
                        executedAt = executedAt,
                    )
                } catch (ex: Exception) {
                    logger.error(
                        """
                        ⚠️ Error ejecutando test ${definition.id} para snippet ${message.snippetId}
                        • Inputs: ${definition.inputs}
                        • Expected outputs: ${definition.outputs}
                        • Exception: ${ex::class.simpleName}: ${ex.message}
                        """.trimIndent(),
                        ex, // ⬅️ imprime el STACKTRACE completo
                    )

                    TestResult(
                        testId = definition.id,
                        status = TestStatus.FAILED,
                        errors = listOf("${ex::class.simpleName}: ${ex.message}"),
                        executedAt = executedAt,
                    )
                }
            }
        }

        private fun persistResults(
            snippetId: Long,
            results: List<TestResult>,
        ) {
            try {
                val serialized = objectMapper.writeValueAsString(results)
                val key = snippetId.toString()
                println("Saving test result for snippetId=$snippetId in container 'test-results' with key=$key")
                assetService.put("test-results", snippetId, serialized)
                println("Successfully saved test result for snippetId=$snippetId")
            } catch (ex: Exception) {
                println("Error saving test result for snippetId=$snippetId: ${ex.message}")
                ex.printStackTrace()
                throw ex
            }
        }
    }
