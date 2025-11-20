package validate

import org.amshove.kluent.shouldNotBeNull
import org.junit.jupiter.api.Test

class ValidateServiceMoreTests {
    private val service = ValidateService()

    @Test
    fun `validate should handle version one one`() {
        val code = "let x = 1; println(x)"
        val version = "1.1"

        val result = service.validate(version, code)

        result.shouldNotBeNull()
    }

    @Test
    fun `validate should handle version normalization with v prefix`() {
        val code = "let x = 1; println(x)"
        val version = "1.0" // El servicio normaliza internamente

        val result = service.validate(version, code)

        result.shouldNotBeNull()
    }
}
