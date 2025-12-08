package runner.formatter

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/v1/formatter")
class FormatterController(
    private val formatterService: FormatterService,
) {
    @PostMapping("/format")
    fun format(
        @RequestBody body: FormatRequest,
    ): ResponseEntity<FormatResponse> {
        val formatted = formatterService.format(body)
        return ResponseEntity.ok(
            FormatResponse(ok = true, formatted = formatted),
        )
    }
}
