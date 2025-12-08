package formatter

import com.fasterxml.jackson.databind.ObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import runner.formatter.FormatRequest
import java.io.File
import java.util.stream.Stream

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Disabled("Disabled until test resources are available")
internal class HttpRequestFormatterTest {
    @LocalServerPort
    private val port: Int = 0

    @Autowired
    private lateinit var restTemplate: TestRestTemplate

    private val objectMapper = ObjectMapper()

    companion object {
        @JvmStatic
        fun formatterTestCases(): Stream<Arguments> {
            val formatterDir = File("src/test/resources/formatter")
            if (!formatterDir.exists()) {
                return Stream.of(Arguments.of("1.0", "dummy", File(".")))
            }
            val cases =
                formatterDir
                    .listFiles { file -> file.isDirectory }
                    ?.flatMap { versionDir ->
                        versionDir.listFiles { file -> file.isDirectory }?.map { subDir ->
                            Arguments.of(versionDir.name, subDir.name, subDir)
                        } ?: emptyList()
                    }?.stream() ?: Stream.empty()
            val casesList = cases.toList()
            return if (casesList.isEmpty()) {
                Stream.of(Arguments.of("1.0", "dummy", File(".")))
            } else {
                casesList.stream()
            }
        }
    }

    @ParameterizedTest(name = "version {0} - {1}")
    @MethodSource("formatterTestCases")
    @DisplayName("Formatter Test Cases")
    @Throws(Exception::class)
    fun `test formatter cases`(
        version: String,
        name: String,
        subDir: File,
    ) {
        if (name == "dummy" || !subDir.exists() || !File(subDir, "code.txt").exists()) {
            return
        }
        val code = File(subDir, "code.txt").readText()
        val rulesJson =
            if (File(subDir, "rules.json").exists()) {
                File(subDir, "rules.json").readText()
            } else {
                "{}"
            }
        val rules = objectMapper.readTree(rulesJson)
        val response =
            if (File(subDir, "response.txt").exists()) {
                File(subDir, "response.txt").readText()
            } else {
                ""
            }

        val rulesMap = objectMapper.convertValue(rules, Map::class.java) as Map<String, Any?>
        val requestBody = FormatRequest(version, code, rulesMap)

        val headers =
            HttpHeaders().apply {
                contentType = MediaType.APPLICATION_JSON
                set("Authorization", "Bearer mocked-jwt-token")
            }

        val entity = HttpEntity(requestBody, headers)

        val url = "http://localhost:$port/api/printscript/format"
        val result = restTemplate.exchange(url, HttpMethod.POST, entity, String::class.java)

        assertThat(result.statusCode).isEqualTo(HttpStatus.OK)
        if (response.isNotEmpty()) {
            assertThat(result.body).contains(response)
        }
    }
}
