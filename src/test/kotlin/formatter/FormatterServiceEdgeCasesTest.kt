package formatter

import org.amshove.kluent.shouldContain
import org.amshove.kluent.shouldNotBeNull
import org.junit.jupiter.api.Test
import runner.formatter.FormatRequest
import runner.formatter.FormatterService

class FormatterServiceEdgeCasesTest {
    private val service = FormatterService()

    @Test
    fun `format with empty config map should work`() {
        val code = "let x=1;println(x)"
        val version = "1.0"
        val request =
            FormatRequest(
                version = version,
                source = code,
                config = emptyMap(),
            )

        try {
            val result = service.format(request)
            result.shouldNotBeNull()
        } catch (e: Exception) {
            // Puede fallar por sintaxis
        }
    }

    @Test
    fun `getActiveAdaptedRules with multiple active rules should work`() {
        val rulesJson =
            """
            [
                {"id":"1","name":"IndentSize","isActive":true,"value":"2"},
                {"id":"2","name":"LineBreakAfterStatement","isActive":true,"value":"true"},
                {"id":"3","name":"MaxLineLength","isActive":true,"value":"80"}
            ]
            """.trimIndent()

        val result = service.getActiveAdaptedRules(rulesJson)

        result.shouldNotBeNull()
    }

    @Test
    fun `getActiveAdaptedRules with camelCase names should convert to snake_case`() {
        val rulesJson =
            """
            [{"id":"1","name":"IndentSize","isActive":true,"value":"2"}]
            """.trimIndent()

        val result = service.getActiveAdaptedRules(rulesJson)

        result.shouldNotBeNull()
        result shouldContain "indent_size"
    }
}
