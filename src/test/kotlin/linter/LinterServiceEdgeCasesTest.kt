package linter

import org.amshove.kluent.shouldNotBeNull
import org.junit.jupiter.api.Test
import runner.linter.LinterService
import runner.types.Rule

class LinterServiceEdgeCasesTest {
    private val service = LinterService()

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
}
