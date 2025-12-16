package interpreter

import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import runner.interpreter.GlobalExceptionHandler

class InterpreterGlobalExceptionHandlerTest {
    private val handler = GlobalExceptionHandler()

    @Test
    fun `should handle exception with message`() {
        val exception = RuntimeException("Test error")

        val response = handler.onAny(exception)

        assert(response.statusCode == HttpStatus.BAD_REQUEST)
        assert(response.body?.ok == false)
        assert(response.body?.message == "Test error")
    }

    @Test
    fun `should handle exception without message`() {
        val exception = RuntimeException()

        val response = handler.onAny(exception)

        assert(response.statusCode == HttpStatus.BAD_REQUEST)
        assert(response.body?.ok == false)
        assert(response.body?.message == "Runtime error")
    }
}
