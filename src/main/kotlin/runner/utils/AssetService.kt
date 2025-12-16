package runner.utils

import org.springframework.stereotype.Service
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.HttpServerErrorException
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestTemplate

@Service
class AssetService(
    private val restTemplate: RestTemplate,
) {
    fun get(
        directory: String,
        id: Long,
    ): String {
        val response =
            restTemplate.getForObject(
                "$ASSETSERVICE_URL/$directory/$id",
                String::class.java,
            )
        return response ?: "Search in $directory not found"
    }

    fun put(
        directory: String,
        id: Long,
        content: String,
    ): String {
        try {
            restTemplate.put(
                "$ASSETSERVICE_URL/$directory/$id",
                content,
                String::class.java,
            )
            return "Snippet updated"
        } catch (ex: HttpClientErrorException) {
            val status = ex.statusCode
            val body = ex.responseBodyAsString
            //  println("Error saving to asset-service: HTTP ${status.value()} ${status.reasonPhrase}")
            println("Response body: $body")
            println("URL: $ASSETSERVICE_URL/$directory/$id")
            throw ex
        } catch (ex: HttpServerErrorException) {
            val status = ex.statusCode
            val body = ex.responseBodyAsString
            //  println("Error saving to asset-service: HTTP ${status.value()} ${status.reasonPhrase}")
            println("Response body: $body")
            println("URL: $ASSETSERVICE_URL/$directory/$id")
            throw ex
        } catch (ex: RestClientException) {
            println("Error saving to asset-service: ${ex.message}")
            println("URL: $ASSETSERVICE_URL/$directory/$id")
            throw ex
        }
    }

    fun delete(
        directory: String,
        id: Long,
    ): String {
        restTemplate.delete(
            "$ASSETSERVICE_URL/$directory/$id",
        )
        return "Snippet deleted"
    }
}
