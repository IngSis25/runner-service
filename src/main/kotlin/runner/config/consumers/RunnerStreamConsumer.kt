package runner.config.consumers

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import kotlinx.serialization.json.JsonObject
import org.austral.ingsis.redis.RedisStreamConsumer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.data.redis.connection.stream.ObjectRecord
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.data.redis.stream.StreamReceiver
import org.springframework.stereotype.Service
import runner.config.SnippetMessage
import runner.formatter.FormatterService
import runner.linter.LinterService
import runner.snippet.SnippetService
import runner.types.Compliance
import runner.types.Rule
import runner.utils.AssetService
import java.time.Duration

@Service
@Profile("!test")
class RunnerStreamConsumer
    @Autowired
    constructor(
        redisTemplate: ReactiveRedisTemplate<String, String>,
        @Value("\${stream.runner.key}") streamKey: String,
        @Value("\${groups.runner}") groupId: String,
        private val formatService: FormatterService,
        private val lintService: LinterService,
        private val assetService: AssetService,
        private val snippetService: SnippetService,
    ) : RedisStreamConsumer<String>(streamKey, groupId, redisTemplate) {
        private val objectMapper = jacksonObjectMapper()

        init {
            println("=== RunnerStreamConsumer initialized ===")
            println("Stream key: $streamKey")
            println("Group ID: $groupId")
        }

        override fun options(): StreamReceiver.StreamReceiverOptions<String, ObjectRecord<String, String>> =
            StreamReceiver.StreamReceiverOptions
                .builder()
                .pollTimeout(Duration.ofMillis(10000))
                .targetType(String::class.java)
                .build()

        public override fun onMessage(record: ObjectRecord<String, String>) {
            println("=== RunnerStreamConsumer received message ===")
            println("Record value: ${record.value}")
            // Cuando se publica un MapRecord, Redis lo serializa como JSON
            // El record.value contiene el JSON serializado del Map
            // Necesitamos parsearlo para obtener los campos "type" y "data"
            try {
                val recordMap = objectMapper.readValue<Map<String, String>>(record.value, object : com.fasterxml.jackson.core.type.TypeReference<Map<String, String>>() {})
                println("Parsed record map: $recordMap")
                val messageType = recordMap["type"] ?: return
                val dataJson = recordMap["data"] ?: return
                println("Message type: $messageType, Data: $dataJson")

                when (messageType) {
                    "format" -> handleFormatMessage(dataJson)
                    "lint" -> handleLintMessage(dataJson)
                    "snippet" -> {
                        // Por compatibilidad, si viene "snippet" lo tratamos como format
                        handleFormatMessage(dataJson)
                    }
                    else -> println("Unknown message type: $messageType")
                }
            } catch (e: Exception) {
                println("Error processing message: ${e.message}")
                e.printStackTrace()
            }
        }

        private fun handleFormatMessage(dataJson: String) {
            // El mensaje viene del snippet-service con userId como String (auth0Id)
            // Necesitamos parsearlo y convertirlo a Long para el runner-service
            val messageMap = objectMapper.readValue<Map<String, Any>>(dataJson, object : com.fasterxml.jackson.core.type.TypeReference<Map<String, Any>>() {})
            val userIdString = messageMap["userId"] as? String ?: return
            val userIdLong = userIdString.hashCode().toLong().and(0x7FFFFFFF)
            
            val message = SnippetMessage(
                snippetId = (messageMap["snippetId"] as? Number)?.toLong() ?: return,
                userId = userIdLong,
                version = messageMap["version"] as? String ?: return,
                jwtToken = messageMap["jwtToken"] as? String ?: return,
            )
            
            try {
                println("=== DEBUG: Formatting snippet ===")
                println("SnippetId: ${message.snippetId}, Version: ${message.version}")
                val formatRules: String = getFormatRulesAsString(message)
                val content = assetService.get("snippets", message.snippetId)
                println("Original content: $content")
                println("Format rules to apply: $formatRules")
                val formattedCode = formatService.format(message.version, content, formatRules)
                println("Formatted code: $formattedCode")
                assetService.put("snippets", message.snippetId, formattedCode)
                println("=== Format completed ===")
            } catch (e: Exception) {
                println("Error formatting: ${e.message}")
                e.printStackTrace()
            }
        }

        private fun handleLintMessage(dataJson: String) {
            // El mensaje viene del snippet-service con userId como String (auth0Id)
            // Necesitamos parsearlo y convertirlo a Long para el runner-service
            val messageMap = objectMapper.readValue<Map<String, Any>>(dataJson, object : com.fasterxml.jackson.core.type.TypeReference<Map<String, Any>>() {})
            val userIdString = messageMap["userId"] as? String ?: return
            val userIdLong = userIdString.hashCode().toLong().and(0x7FFFFFFF)
            
            val message = SnippetMessage(
                snippetId = (messageMap["snippetId"] as? Number)?.toLong() ?: return,
                userId = userIdLong,
                version = messageMap["version"] as? String ?: return,
                jwtToken = messageMap["jwtToken"] as? String ?: return,
            )
            
            try {
                val lintRules: JsonObject = getLintRulesAsJsonObject(message)
                val content = assetService.get("snippets", message.snippetId)
                val warnings = lintService.analyze(message.version, content, lintRules)
                val success = warnings.isEmpty()
                snippetService.updateStatus(
                    message.jwtToken,
                    message.snippetId,
                    if (success) Compliance.SUCCESS else Compliance.FAILED,
                )

                val warningsJson = objectMapper.writeValueAsString(warnings)
                assetService.put("lint-warnings", message.snippetId, warningsJson)
            } catch (e: Exception) {
                println("Error linting: ${e.message}")
                e.printStackTrace()
                snippetService.updateStatus(message.jwtToken, message.snippetId, Compliance.FAILED)
            }
        }

        private fun getFormatRulesAsString(message: SnippetMessage): String =
            try {
                println("=== DEBUG: Getting format rules ===")
                println("UserId: ${message.userId}")
                val formatRulesJson = assetService.get("format-rules", message.userId)
                println("Raw format rules JSON from asset service: $formatRulesJson")
                val adaptedRules = formatService.getActiveAdaptedRules(formatRulesJson)
                println("Adapted rules JSON: $adaptedRules")
                adaptedRules
            } catch (e: Exception) {
                println("Error deserializing format rules: ${e.message}")
                e.printStackTrace()
                "{}"
            }

        private fun getLintRulesAsJsonObject(message: SnippetMessage): JsonObject =
            try {
                val lintRulesJson = assetService.get("lint-rules", message.userId)
                val lintRules: List<Rule> = objectMapper.readValue(lintRulesJson, object : com.fasterxml.jackson.core.type.TypeReference<List<Rule>>() {})
                lintService.convertActiveRulesToJsonObject(lintRules)
            } catch (e: Exception) {
                println("Error deserializing lint rules: ${e.message}")
                JsonObject(emptyMap())
            }
    }

