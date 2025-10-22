package app

import app.domain.model.VersionMismatchException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class ApiExceptionHandler {

    data class ErrorRes(val status: Int, val error: String, val message: String)

    @ExceptionHandler(VersionMismatchException::class)
    fun handleVersionMismatch(ex: VersionMismatchException): ResponseEntity<ErrorRes> {
        val body = ErrorRes(
            status = HttpStatus.CONFLICT.value(),
            error = "Version Mismatch",
            message = ex.message ?: "Version mismatch"
        )
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body)
    }
}
