package config.consumers

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import config.SnippetMessage
import formatter.FormatterService
import org.austral.ingsis.redis.RedisStreamConsumer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.data.redis.connection.stream.ObjectRecord
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.data.redis.stream.StreamReceiver
import org.springframework.stereotype.Service
import utils.AssetService
import java.time.Duration

@Service
@Profile("!test")
class FormatRuleConsumer
    @Autowired
    constructor(
        redisTemplate: ReactiveRedisTemplate<String, String>,
        @Value("\${stream.format.key}") streamKey: String,
        @Value("\${groups.format}") groupId: String,
        private val formatService: FormatterService,
        private val assetService: AssetService,
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
                val formatRules: String = getRulesAsString(message)
                val content = assetService.get("snippets", message.snippetId)
                val formattedCode = formatService.format(message.version, content, formatRules)
                assetService.put("snippets", message.snippetId, formattedCode)
            } catch (e: Exception) {
                println("Error formatting: ${e.message}")
            }
        }

        private fun getRulesAsString(message: SnippetMessage): String =
            try {
                val formatRulesJson = assetService.get("format-rules", message.userId)
                formatService.getActiveAdaptedRules(formatRulesJson)
            } catch (e: Exception) {
                println("Error deserializing format rules: ${e.message}")
                "{}"
            }
    }
