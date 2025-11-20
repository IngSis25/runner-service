package interpreter

import com.fasterxml.jackson.databind.ObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import utils.AssetService
import java.io.File
import java.util.stream.Stream

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Disabled("Disabled until test resources are available")
internal class HttpRequestInterpreterTest {
    @LocalServerPort
    private val port: Int = 0

    @Autowired
    private lateinit var restTemplate: TestRestTemplate

    @MockBean
    private lateinit var assetService: AssetService

    private val objectMapper = ObjectMapper()

    @BeforeEach
    fun setUp() {
        org.mockito.kotlin
            .whenever(assetService.get("snippets", 1L))
            .thenReturn("println(1);")
    }

    companion object {
        @JvmStatic
        fun interpreterTestCases(): Stream<Arguments> {
            val interpreterDir = File("src/test/resources/interpreter")
            if (!interpreterDir.exists()) {
                return Stream.of(Arguments.of("1.0", "dummy", File(".")))
            }
            val cases =
                interpreterDir
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

        @JvmStatic
        fun testEndpointTestCases(): Stream<Arguments> {
            val testDir = File("src/test/resources/test")
            if (!testDir.exists()) {
                return Stream.of(Arguments.of("1.0", "dummy", File(".")))
            }
            val cases =
                testDir
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
    @MethodSource("interpreterTestCases")
    @DisplayName("Interpreter Test Cases")
    @Throws(Exception::class)
    fun `test interpreter cases`(
        version: String,
        name: String,
        subDir: File,
    ) {
        if (name == "dummy" || !subDir.exists() || !File(subDir, "code.txt").exists()) {
            return
        }
        val code = File(subDir, "code.txt").readText()
        val response =
            if (File(subDir, "response.txt").exists()) {
                File(subDir, "response.txt").readText()
            } else {
                ""
            }

        val requestBody = InterpretDto(version, code)

        val headers =
            HttpHeaders().apply {
                contentType = MediaType.APPLICATION_JSON
                set("Authorization", "Bearer mocked-jwt-token")
            }

        val entity = HttpEntity(requestBody, headers)

        val url = "http://localhost:$port/api/printscript/interpret"
        val result = restTemplate.exchange(url, HttpMethod.POST, entity, String::class.java)

        assertThat(result.statusCode).isEqualTo(HttpStatus.OK)
        if (response.isNotEmpty()) {
            assertThat(result.body).contains(response)
        }
    }

    @ParameterizedTest(name = "version {0} - {1}")
    @MethodSource("testEndpointTestCases")
    @DisplayName("Test Endpoint Test Cases")
    @Throws(Exception::class)
    fun `test endpoint cases`(
        version: String,
        name: String,
        subDir: File,
    ) {
        if (name == "dummy" || !subDir.exists() || !File(subDir, "code.txt").exists()) {
            return
        }
        val snippetId = File(subDir, "code.txt").readText().toLong()
        val inputs =
            if (File(subDir, "inputs.txt").exists()) {
                File(subDir, "inputs.txt").readLines()
            } else {
                emptyList()
            }
        val outputs =
            if (File(subDir, "outputs.txt").exists()) {
                File(subDir, "outputs.txt").readLines()
            } else {
                emptyList()
            }

        whenever(assetService.get("snippets", snippetId)).thenReturn("println(1);")

        val requestBody = TestDto(version, snippetId, inputs, outputs)

        val headers =
            HttpHeaders().apply {
                contentType = MediaType.APPLICATION_JSON
                set("Authorization", "Bearer mocked-jwt-token")
            }

        val entity = HttpEntity(requestBody, headers)

        val url = "http://localhost:$port/api/printscript/test"
        val result = restTemplate.exchange(url, HttpMethod.POST, entity, String::class.java)

        assertThat(result.statusCode).isEqualTo(HttpStatus.OK)
    }
}
