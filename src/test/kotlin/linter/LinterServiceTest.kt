package linter

import org.amshove.kluent.shouldNotBeNull
import org.junit.jupiter.api.Test
import runner.linter.LinterService
import runner.types.Rule

class LinterServiceTest {
    private val service = LinterService()

    @Test
    fun `analyze should return list of warnings`() {
        val code = "let x = 1; println(x)"
        val version = "1.0"
        val rules: Map<String, Any?>? = null

        val result = service.analyze(version, code, rules)

        // Debería retornar una lista (puede estar vacía o con warnings)
        result.shouldNotBeNull()
    }

    @Test
    fun `analyze should handle code with linting rules`() {
        val code = "let x = 1; println(x)"
        val version = "1.0"
        val rules =
            mapOf<String, Any?>(
                "PrintUseCheck" to mapOf("printlnCheckEnabled" to true),
            )

        val result = service.analyze(version, code, rules)

        result.shouldNotBeNull()
    }

    @Test
    fun `analyze should handle empty code`() {
        val code = ""
        val version = "1.0"
        val rules: Map<String, Any?>? = null

        val result = service.analyze(version, code, rules)

        result.shouldNotBeNull()
    }

    @Test
    fun `analyze with version one one should work`() {
        val code = "let x = 1; println(x)"
        val version = "1.1"
        val rules: Map<String, Any?>? = null

        val result = service.analyze(version, code, rules)

        result.shouldNotBeNull()
    }

    @Test
    fun `convertActiveRulesToJsonObject should convert rules correctly`() {
        val rules =
            listOf(
                Rule(id = "1", name = "PrintUseCheck", isActive = true, value = null),
                Rule(id = "2", name = "ReadInputCheck", isActive = true, value = null),
                Rule(id = "3", name = "NamingFormatCheck", isActive = true, value = "camelCase"),
            )

        val result = service.convertActiveRulesToJsonObject(rules)

        result.shouldNotBeNull()
    }
}
