package linter

import io.mockk.every
import io.mockk.mockk
import org.amshove.kluent.shouldNotBeNull
import org.junit.jupiter.api.Test

class LinterControllerTest {
    private val linterService = mockk<LinterService>(relaxed = true)
    private val controller = LinterController(linterService)

    @Test
    fun `lint should call service and return result`() {
        val request =
            LinterDto(
                version = "1.0",
                code = "let x = 1; println(x)",
                rules = null,
            )

        every { linterService.analyze("1.0", "let x = 1; println(x)", null) } returns emptyList()

        val result = controller.lintCode(request)

        result.shouldNotBeNull()
    }
}
