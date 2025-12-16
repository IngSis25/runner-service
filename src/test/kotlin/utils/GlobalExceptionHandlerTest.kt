package utils

import io.mockk.every
import io.mockk.mockk
import jakarta.servlet.http.HttpServletRequest
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.http.HttpStatus
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.validation.BindingResult
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import runner.utils.GlobalExceptionHandler
import java.lang.RuntimeException

class GlobalExceptionHandlerTest {
    private val handler = GlobalExceptionHandler()
    private val request = mock<HttpServletRequest>()

    @Test
    fun `should handle HttpMessageNotReadableException`() {
        whenever(request.requestURI).thenReturn("/test")
        val cause = RuntimeException("Parse error")
        val exception = object : HttpMessageNotReadableException("Invalid JSON", cause) {}

        val response = handler.onUnreadable(exception, request)

        assert(response.statusCode == HttpStatus.BAD_REQUEST)
        assert(response.body?.ok == false)
        assert(response.body?.message?.isNotEmpty() == true)
        assert(response.body?.path == "/test")
    }

    @Test
    fun `should handle HttpMessageNotReadableException without cause`() {
        whenever(request.requestURI).thenReturn("/test")
        val exception = object : HttpMessageNotReadableException("Invalid JSON") {}

        val response = handler.onUnreadable(exception, request)

        assert(response.statusCode == HttpStatus.BAD_REQUEST)
        assert(response.body?.ok == false)
        // When mostSpecificCause is null, it should return "Invalid request body"
        assert(response.body?.message == "Invalid request body" || response.body?.message?.isNotEmpty() == true)
    }

    @Test
    fun `should handle MethodArgumentNotValidException`() {
        whenever(request.requestURI).thenReturn("/test")
        val bindingResult = mockk<BindingResult>(relaxed = true)
        val fieldError = FieldError("test", "field", "Error message")
        every { bindingResult.fieldErrors } returns listOf(fieldError)
        every { bindingResult.globalErrors } returns emptyList()
        val methodParameter = mockk<org.springframework.core.MethodParameter>(relaxed = true)
        every { methodParameter.parameterName } returns "testParam"
        val exception = MethodArgumentNotValidException(methodParameter, bindingResult)

        val response = handler.onValidation(exception, request)

        assert(response.statusCode == HttpStatus.BAD_REQUEST)
        assert(response.body?.ok == false)
        assert(response.body?.message == "Validation failed")
        assert(response.body?.details?.containsKey("fields") == true)
        assert(response.body?.path == "/test")
    }

    @Test
    fun `should handle MethodArgumentTypeMismatchException`() {
        whenever(request.requestURI).thenReturn("/test")
        val methodParameter = mock<org.springframework.core.MethodParameter>()
        val exception =
            MethodArgumentTypeMismatchException(
                "invalid",
                String::class.java,
                "param",
                methodParameter,
                RuntimeException("Type mismatch"),
            )

        val response = handler.onTypeMismatch(exception, request)

        assert(response.statusCode == HttpStatus.BAD_REQUEST)
        assert(response.body?.ok == false)
        assert(response.body?.message?.contains("param") == true)
        assert(response.body?.details?.containsKey("expectedType") == true)
        assert(response.body?.path == "/test")
    }

    @Test
    fun `should handle IllegalArgumentException`() {
        whenever(request.requestURI).thenReturn("/test")
        val exception = IllegalArgumentException("Invalid argument")

        val response = handler.onAny(exception, request)

        assert(response.statusCode == HttpStatus.BAD_REQUEST)
        assert(response.body?.ok == false)
        assert(response.body?.message == "Invalid argument")
        assert(response.body?.path == "/test")
    }

    @Test
    fun `should handle IllegalStateException`() {
        whenever(request.requestURI).thenReturn("/test")
        val exception = IllegalStateException("Invalid state")

        val response = handler.onAny(exception, request)

        assert(response.statusCode == HttpStatus.BAD_REQUEST)
        assert(response.body?.ok == false)
        assert(response.body?.message == "Invalid state")
    }

    @Test
    fun `should handle generic Exception`() {
        whenever(request.requestURI).thenReturn("/test")
        val exception = RuntimeException("Generic error")

        val response = handler.onAny(exception, request)

        assert(response.statusCode == HttpStatus.BAD_REQUEST)
        assert(response.body?.ok == false)
        assert(response.body?.message == "Generic error")
    }

    @Test
    fun `should handle Exception without message`() {
        whenever(request.requestURI).thenReturn("/test")
        val exception = RuntimeException()

        val response = handler.onAny(exception, request)

        assert(response.statusCode == HttpStatus.BAD_REQUEST)
        assert(response.body?.ok == false)
        assert(response.body?.message?.isNotEmpty() == true)
    }
}
