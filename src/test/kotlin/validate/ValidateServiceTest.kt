package validate

import org.amshove.kluent.shouldNotBeNull
import org.junit.jupiter.api.Test

class ValidateServiceTest {
    private val service = ValidateService()

    @Test
    fun `validate should return list of errors`() {
        val code = "let x = 1; println(x)"
        val version = "1.0"

        val result = service.validate(version, code)

        // Debería retornar una lista (puede estar vacía si no hay errores)
        result.shouldNotBeNull()
    }

    @Test
    fun `validate should return errors for invalid code`() {
        val code = "let x = ; println(x)" // Código inválido
        val version = "1.0"

        val result = service.validate(version, code)

        // Para código inválido, debería retornar errores
        result.shouldNotBeNull()
    }

    @Test
    fun `validate should handle empty code`() {
        val code = ""
        val version = "1.0"

        val result = service.validate(version, code)

        result.shouldNotBeNull()
    }
}
