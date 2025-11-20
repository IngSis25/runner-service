package config.consumers

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import config.SnippetMessage
import kotlinx.serialization.json.JsonObject
import linter.LinterService
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
import types.Rule
import utils.AssetService
import java.time.Duration

@Service
@Profile("!test")
class LinterRuleConsumer
    @Autowired
    constructor(
        redisTemplate: ReactiveRedisTemplate<String, String>,
        @Value("\${stream.lint.key}") streamKey: String,
        @Value("\${groups.lint}") groupId: String,
        private val lintService: LinterService,
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
            val message: SnippetMessage = objectMapper.readValue(record.value, SnippetMessage::class.java)
            try {
                val lintRules: JsonObject = getRulesAsJsonObject(message)
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
                snippetService.updateStatus(message.jwtToken, message.snippetId, Compliance.FAILED)
            }
        }

        private fun getRulesAsJsonObject(message: SnippetMessage): JsonObject =
            try {
                val lintRulesJson = assetService.get("lint-rules", message.userId)
                val lintRules: List<Rule> = objectMapper.readValue(lintRulesJson, object : TypeReference<List<Rule>>() {})
                lintService.convertActiveRulesToJsonObject(lintRules)
            } catch (e: Exception) {
                println("Error deserializing lint rules: ${e.message}")
                JsonObject(emptyMap())
            }
    }
