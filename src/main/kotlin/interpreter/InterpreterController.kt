package interpreter

import factory.LexerFactoryRegistry
import main.kotlin.parser.ConfiguredRules
import main.kotlin.parser.DefaultParser
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import rules.ParserRule
import rules.RuleMatcher

@RestController
@RequestMapping("/v1/interpreter")
class InterpreterController(
    private val interpreterService: InterpreterService,
) {
    @PostMapping("/run")
    fun run(
        @RequestBody body: RunRequest,
    ): ResponseEntity<RunResponse> {
        val lexer = LexerFactoryRegistry.getFactory(body.version).create()

        val parser =
            when (body.version) {
                "1.0" -> {
                    val rules: List<ParserRule> = ConfiguredRules.V1
                    DefaultParser(RuleMatcher(rules))
                }
                "1.1" -> {
                    val dummy = DefaultParser(RuleMatcher(emptyList<ParserRule>()))
                    val rules = ConfiguredRules.createV11Rules(dummy)
                    DefaultParser(RuleMatcher(rules))
                }
                else -> error("Unsupported PrintScript version: ${body.version}")
            }
        val result = interpreterService.run(body.version, body.source, lexer, parser)
        return ResponseEntity.ok(
            RunResponse(ok = true, output = result.output, diagnostics = result.diagnostics),
        )
    }
}
