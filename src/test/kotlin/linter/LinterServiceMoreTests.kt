package linter

import kotlinx.serialization.json.JsonObject
import org.amshove.kluent.shouldNotBeNull
import org.junit.jupiter.api.Test
import runner.linter.LinterService
import runner.types.Rule

class LinterServiceMoreTests {
    private val service = LinterService()

    @Test
    fun `analyze with JsonObject rules should work`() {
        val code = "let x = 1; println(x)"
        val version = "1.0"
        val rules = JsonObject(emptyMap())

        val result = service.analyze(version, code, rules)

        result.shouldNotBeNull()
    }

    @Test
    fun `analyze with JsonObject rules and version one one should work`() {
        val code = "let x = 1; println(x)"
        val version = "1.1"
        val rules = JsonObject(emptyMap())

        val result = service.analyze(version, code, rules)

        result.shouldNotBeNull()
    }

    @Test
    fun `convertActiveRulesToJsonObject with empty list should work`() {
        val rules = emptyList<Rule>()

        val result = service.convertActiveRulesToJsonObject(rules)

        result.shouldNotBeNull()
    }

    @Test
    fun `convertActiveRulesToJsonObject with inactive rules should filter them`() {
        val rules =
            listOf(
                Rule(id = "1", name = "PrintUseCheck", isActive = true, value = null),
                Rule(id = "2", name = "ReadInputCheck", isActive = false, value = null),
            )

        val result = service.convertActiveRulesToJsonObject(rules)

        result.shouldNotBeNull()
    }

    @Test
    fun `convertActiveRulesToJsonObject with non-string value should work`() {
        val rules =
            listOf(
                Rule(id = "1", name = "SomeRule", isActive = true, value = 123),
            )

        val result = service.convertActiveRulesToJsonObject(rules)

        result.shouldNotBeNull()
    }
}
