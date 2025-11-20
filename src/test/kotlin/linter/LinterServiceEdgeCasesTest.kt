package linter

import kotlinx.serialization.json.JsonObject
import org.amshove.kluent.shouldNotBeNull
import org.junit.jupiter.api.Test
import types.Rule

class LinterServiceEdgeCasesTest {
    private val service = LinterService()

    @Test
    fun `analyze with non-empty Map rules should convert correctly`() {
        val code = "let x = 1; println(x)"
        val version = "1.0"
        val rules =
            mapOf<String, Any?>(
                "PrintUseCheck" to mapOf("printlnCheckEnabled" to true),
                "ReadInputCheck" to mapOf("readInputCheckEnabled" to false),
            )

        val result = service.analyze(version, code, rules)

        result.shouldNotBeNull()
    }

    @Test
    fun `convertActiveRulesToJsonObject with all rule types should work`() {
        val rules =
            listOf(
                Rule(id = "1", name = "PrintUseCheck", isActive = true, value = null),
                Rule(id = "2", name = "ReadInputCheck", isActive = true, value = null),
                Rule(id = "3", name = "NamingFormatCheck", isActive = true, value = "camelCase"),
                Rule(id = "4", name = "OtherRule", isActive = true, value = "someValue"),
                Rule(id = "5", name = "NumericRule", isActive = true, value = 123),
            )

        val result = service.convertActiveRulesToJsonObject(rules)

        result.shouldNotBeNull()
    }

    @Test
    fun `analyze with JsonObject containing data should work`() {
        val code = "let x = 1; println(x)"
        val version = "1.0"
        val rules =
            JsonObject(
                mapOf(
                    "PrintUseCheck" to kotlinx.serialization.json.JsonPrimitive("true"),
                ),
            )

        val result = service.analyze(version, code, rules)

        result.shouldNotBeNull()
    }
}
