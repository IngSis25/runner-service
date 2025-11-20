package tests

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.stereotype.Service
import types.TestResult
import utils.AssetService

@Service
class TestResultService(
    private val assetService: AssetService,
) {
    private val objectMapper =
        jacksonObjectMapper()
            .registerModule(JavaTimeModule())

    fun getResults(snippetId: Long): List<TestResult> =
        try {
            val payload = assetService.get("test-results", snippetId)
            if (payload.isBlank()) {
                emptyList()
            } else {
                objectMapper.readValue(payload, object : TypeReference<List<TestResult>>() {})
            }
        } catch (ex: Exception) {
            emptyList()
        }
}
