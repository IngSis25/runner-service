package runner.validate

import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/printscript")
class ValidateController(
    private val validateService: ValidateService,
) {
    @PostMapping("/validate")
    fun validate(
        @RequestBody validateDto: ValidateDto,
    ): List<String> = validateService.validate(validateDto.version, validateDto.code)
}
