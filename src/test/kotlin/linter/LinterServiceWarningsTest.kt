package linter

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import runner.linter.LinterService

class LinterServiceWarningsTest {
    private val service = LinterService()

    @Test
    fun `lint should report warnings for unused and bad naming`() {
        val code =
            """
            let MiVariable: number = 10;
            let otraVariable: number = 30;
            let variable_no_usada: number = 30;
            println(otraVariable);
            """.trimIndent()

        val rules =
            mapOf<String, Any?>(
                "UnusedVariableCheck" to null,
                "NamingFormatCheck" to mapOf("namingPatternName" to "camelCase"),
            )

        val warnings = service.analyze("1.1", code, rules)

        // Debería haber al menos una advertencia (unused y/o naming)
        assertTrue(warnings.isNotEmpty(), "Se esperaban warnings, pero el linter devolvió vacío")
    }
}
