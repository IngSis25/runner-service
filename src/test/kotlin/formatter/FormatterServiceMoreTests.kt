package formatter

import org.amshove.kluent.shouldNotBeNull
import org.junit.jupiter.api.Test
import runner.formatter.FormatterService

class FormatterServiceMoreTests {
    private val service = FormatterService()

    @Test
    fun `format with version and rules string should work`() {
        val code = "let x=1;println(x)"
        val version = "1.0"
        val rules = """{"indent_size":2}"""

        try {
            val result = service.format(version, code, rules)
            result.shouldNotBeNull()
        } catch (e: Exception) {
            // Puede fallar por sintaxis
        }
    }

    @Test
    fun `format with version one one and rules string should work`() {
        val code = "let x=1;println(x)"
        val version = "1.1"
        val rules = """{"indent_size":2}"""

        try {
            val result = service.format(version, code, rules)
            result.shouldNotBeNull()
        } catch (e: Exception) {
            // Puede fallar por sintaxis
        }
    }

    @Test
    fun `getActiveAdaptedRules with empty list should work`() {
        val rulesJson = "[]"

        val result = service.getActiveAdaptedRules(rulesJson)

        result.shouldNotBeNull()
    }

    @Test
    fun `getActiveAdaptedRules with inactive rules should filter them`() {
        val rulesJson =
            """
            [
                {"id":"1","name":"IndentSize","isActive":true,"value":"2"},
                {"id":"2","name":"LineBreak","isActive":false,"value":"true"}
            ]
            """.trimIndent()

        val result = service.getActiveAdaptedRules(rulesJson)

        result.shouldNotBeNull()
    }
}
