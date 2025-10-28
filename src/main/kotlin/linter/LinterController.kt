package linter

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import kotlin.code

@RestController
class LinterController(
    private val linterService: LinterService,
) {
    @PostMapping("/lint")
    fun lintCode(
        @RequestBody linterDto: LinterDto,
    ): List<String> =
        linterService.analyze(
            linterDto.version,
            linterDto.code,
            linterDto.rules,
        )

    @RestController
    class PingController {
        @GetMapping("/ping")
        fun ping() = "pong"
    }
}
