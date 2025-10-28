package linter

import main.kotlin.analyzer.AnalyzerConfig

data class LinterDto(
    val version: String,
    val code: String,
    val rules: AnalyzerConfig,
)
