package interpreter

import main.kotlin.lexer.Lexer
import main.kotlin.parser.DefaultParser
import org.example.DefaultInterpreter
import org.example.ast.ASTNode
import org.example.strategy.StrategyProvider
import org.springframework.stereotype.Service

@Service
class InterpreterService(
    private val providerFactory: (String) -> StrategyProvider,
) {
    fun run(
        version: String,
        source: String,
        lexer: Lexer,
        parser: DefaultParser,
    ): RunResult {
        val tokens = lexer.tokenize(source)
        val ast: List<ASTNode> = parser.parse(tokens).toList()

        val out = BufferOutput()
        val provider = providerFactory(version)
        val engine = DefaultInterpreter(out, provider)

        ast.forEach { engine.interpret(it) }

        return RunResult(output = out.content(), diagnostics = emptyList())
    }
}
