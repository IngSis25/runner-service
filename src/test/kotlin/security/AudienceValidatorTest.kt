package security

import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.security.oauth2.jwt.Jwt
import runner.security.AudienceValidator

class AudienceValidatorTest {
    @Test
    fun `should validate JWT with correct audience`() {
        val audience = "test-audience"
        val validator = AudienceValidator(audience)
        val jwt = mock<Jwt>()

        whenever(jwt.audience).thenReturn(listOf(audience, "other-audience"))

        val result = validator.validate(jwt)

        assert(result.errors.isEmpty())
    }

    @Test
    fun `should reject JWT without correct audience`() {
        val audience = "test-audience"
        val validator = AudienceValidator(audience)
        val jwt = mock<Jwt>()

        whenever(jwt.audience).thenReturn(listOf("other-audience"))

        val result = validator.validate(jwt)

        assert(result.errors.isNotEmpty())
        assert(result.errors.first().errorCode == "invalid_token")
    }

    @Test
    fun `should reject JWT with empty audience`() {
        val audience = "test-audience"
        val validator = AudienceValidator(audience)
        val jwt = mock<Jwt>()

        whenever(jwt.audience).thenReturn(emptyList())

        val result = validator.validate(jwt)

        assert(result.errors.isNotEmpty())
    }
}
