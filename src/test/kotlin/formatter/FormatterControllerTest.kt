package formatter

import io.mockk.every
import io.mockk.mockk
import org.amshove.kluent.shouldNotBeNull
import org.junit.jupiter.api.Test
import runner.formatter.FormatRequest
import runner.formatter.FormatterController
import runner.formatter.FormatterService

class FormatterControllerTest {
    private val formatterService = mockk<FormatterService>(relaxed = true)
    private val controller = FormatterController(formatterService)

    @Test
    fun `format should call service and return result`() {
        val request =
            FormatRequest(
                version = "1.0",
                source = "let x=1;println(x)",
                config = null,
            )

        every { formatterService.format(request) } returns "let x = 1; println(x)"

        val result = controller.format(request)

        result.shouldNotBeNull()
    }
}
