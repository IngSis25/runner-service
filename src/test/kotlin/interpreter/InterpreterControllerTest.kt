package interpreter

import io.mockk.every
import io.mockk.mockk
import org.amshove.kluent.shouldNotBeNull
import org.junit.jupiter.api.Test
import runner.interpreter.InterpretDto
import runner.interpreter.InterpreterController
import runner.interpreter.InterpreterService
import runner.interpreter.TestDto

class InterpreterControllerTest {
    private val interpreterService = mockk<InterpreterService>(relaxed = true)
    private val controller = InterpreterController(interpreterService)

    @Test
    fun `interpret should call service and return result`() {
        val request = InterpretDto(version = "1.0", code = "println(\"Hello\")")

        every { interpreterService.interpret("1.0", "println(\"Hello\")") } returns listOf("Hello")

        val result = controller.interpret(request)

        result.shouldNotBeNull()
    }

    @Test
    fun `test should call service and return result`() {
        val request =
            TestDto(
                version = "1.0",
                snippetId = 1L,
                inputs = emptyList(),
                outputs = listOf("Hello"),
            )

        every {
            interpreterService.test("1.0", 1L, emptyList(), listOf("Hello"))
        } returns emptyList()

        val result = controller.test(request)

        result.shouldNotBeNull()
    }
}
