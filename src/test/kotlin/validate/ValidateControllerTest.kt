package validate

import io.mockk.every
import io.mockk.mockk
import org.amshove.kluent.shouldNotBeNull
import org.junit.jupiter.api.Test

class ValidateControllerTest {
    private val validateService = mockk<ValidateService>(relaxed = true)
    private val controller = ValidateController(validateService)

    @Test
    fun `validate should call service and return result`() {
        val request =
            ValidateDto(
                version = "1.0",
                code = "let x = 1; println(x)",
            )

        every { validateService.validate("1.0", "let x = 1; println(x)") } returns emptyList()

        val result = controller.validate(request)

        result.shouldNotBeNull()
    }
}
