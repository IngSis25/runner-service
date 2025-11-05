package utils

import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException

data class ErrorBody(
    val ok: Boolean = false,
    val message: String,
    val details: Map<String, Any?> = emptyMap(),
    val path: String? = null,
)

@ControllerAdvice
class GlobalExceptionHandler {
    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun onUnreadable(
        e: HttpMessageNotReadableException,
        req: HttpServletRequest,
    ): ResponseEntity<ErrorBody> {
        e.printStackTrace()
        val body =
            ErrorBody(
                message = e.mostSpecificCause?.message ?: "Invalid request body",
                path = req.requestURI,
            )
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body)
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun onValidation(
        e: MethodArgumentNotValidException,
        req: HttpServletRequest,
    ): ResponseEntity<ErrorBody> {
        e.printStackTrace()
        val fieldErrors = e.bindingResult.fieldErrors.associate { it.field to (it.defaultMessage ?: "Invalid") }
        val globalErrors = e.bindingResult.globalErrors.map { it.defaultMessage ?: "Invalid" }
        val body =
            ErrorBody(
                message = "Validation failed",
                details =
                    mapOf(
                        "fields" to fieldErrors,
                        "global" to globalErrors,
                    ),
                path = req.requestURI,
            )
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body)
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    fun onTypeMismatch(
        e: MethodArgumentTypeMismatchException,
        req: HttpServletRequest,
    ): ResponseEntity<ErrorBody> {
        e.printStackTrace()
        val body =
            ErrorBody(
                message = "Parameter '${e.name}' has invalid value '${e.value}'",
                details = mapOf("expectedType" to (e.requiredType?.simpleName ?: "unknown")),
                path = req.requestURI,
            )
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body)
    }

    @ExceptionHandler(Exception::class)
    fun onAny(
        e: Exception,
        req: HttpServletRequest,
    ): ResponseEntity<ErrorBody> {
        e.printStackTrace()
        val status =
            if (e is IllegalArgumentException ||
                e is IllegalStateException
            ) {
                HttpStatus.BAD_REQUEST
            } else {
                HttpStatus.BAD_REQUEST
            }
        val body =
            ErrorBody(
                message = e.message ?: e::class.simpleName ?: "Runtime error",
                path = req.requestURI,
            )
        return ResponseEntity.status(status).body(body)
    }
}
