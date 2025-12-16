package runner.analyzer

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/analyzer")
class AnalyzerController(
    private val analyzerService: AnalyzerService,
) {
    @PostMapping("/analyze")
    fun analyze(
        @RequestBody body: AnalyzeRequest,
    ): ResponseEntity<AnalyzeResponse> {
        val diags = analyzerService.analyze(body)
        return ResponseEntity.ok(AnalyzeResponse(ok = true, diagnostics = diags))
    }
}
