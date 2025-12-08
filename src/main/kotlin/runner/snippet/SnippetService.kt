package runner.snippet

import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import runner.types.Compliance

@Service
class SnippetService(
    private val restTemplate: RestTemplate,
) {
    fun updateStatus(
        jwtToken: String,
        id: Long,
        status: Compliance,
    ) {
        val headers =
            HttpHeaders().apply {
                contentType = MediaType.APPLICATION_JSON
                set("Authorization", jwtToken)
            }
        val entity = HttpEntity(status, headers)

        restTemplate.exchange(
            "http://snippet-api:8080/api/snippets/$id/status",
            HttpMethod.PUT,
            entity,
            Void::class.java,
        )
    }
}
