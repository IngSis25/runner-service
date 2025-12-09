package runner.linter

import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import kotlin.code

@RestController
@RequestMapping("/v1/lint")
class LinterController(
    private val linterService: LinterService,
) {
    @PostMapping
    fun lintCode(
        @RequestBody linterDto: LinterDto,
    ): List<String> =
        linterService.analyze(
            linterDto.version,
            linterDto.code,
            linterDto.rules,
        )
}
