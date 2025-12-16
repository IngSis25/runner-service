package runner.interpreter

import main.kotlin.lexer.LexerFactory
import org.ParserFactory
import org.example.InterpreterFactory
import org.springframework.stereotype.Service
import runner.types.TestResult
import runner.types.TestStatus
import runner.utils.AssetService
import java.io.StringReader
import java.time.Instant
import java.util.LinkedList
import kotlin.math.min

@Service
class InterpreterService(
    private val assetService: AssetService,
) {
    /**
     * Interpret the given code and return a list with all the outputs
     * @param version the version of the language
     * @param code the code snippet to interpret
     * @return a list with all the outputs
     */
    fun interpret(
        version: String,
        code: String,
    ): List<String> = runSnippet(version, code, inputs = emptyList())

    /**
     * Test the given code with the given inputs and return a list with all the results
     * @param version the version of the language
     * @param snippetId the id of the snippet to test
     * @param inputs the inputs to test the code with
     * @param expectedOutputs the expected outputs
     * @return a list with all the errors. If there are no errors, the list will be empty
     */
    fun test(
        version: String,
        snippetId: Long,
        inputs: List<String>,
        expectedOutputs: List<String>,
    ): List<String> {
        // 1) Traigo el código del snippet desde asset-service
        val code = assetService.get("snippets", snippetId)

        // 2) Ejecuto el snippet con los inputs del test
        val actualOutputs = runSnippet(version, code, inputs)

        // 3) Comparo outputs reales vs esperados
        return compareOutputs(actualOutputs, expectedOutputs)
    }

    /**
     * Execute a test with code provided directly (no dependency on asset-service or snippetId).
     * This is a pure function that executes the interpreter and compares outputs.
     *
     * @param version the version of the language
     * @param code the code snippet to test
     * @param inputs the inputs to test the code with (can be empty)
     * @param expectedOutputs the expected outputs
     * @return TestResult with status (PASSED/FAILED) and list of error messages
     */
    fun executeTest(
        version: String,
        code: String,
        inputs: List<String>?,
        expectedOutputs: List<String>?,
    ): TestResult {
        val executedAt = Instant.now()

        try {
            // Execute the snippet with the provided inputs
            val actualOutputs = runSnippet(version, code, inputs ?: emptyList())

            // Compare outputs
            val errors = compareOutputs(actualOutputs, expectedOutputs ?: emptyList())

            return TestResult(
                testId = 0L, // Not used in this context
                status = if (errors.isEmpty()) TestStatus.PASSED else TestStatus.FAILED,
                errors = errors,
                executedAt = executedAt,
            )
        } catch (ex: Exception) {
            return TestResult(
                testId = 0L,
                status = TestStatus.FAILED,
                errors = listOf("${ex::class.simpleName}: ${ex.message ?: "Unknown error"}"),
                executedAt = executedAt,
            )
        }
    }

    /**
     * Ejecuta un snippet y devuelve todos los outputs producidos por PrintScript.
     */
    private fun runSnippet(
        version: String,
        code: String,
        inputs: List<String>,
    ): List<String> {
        val reader = StringReader(code)

        // Crear lexer según versión
        val lexer =
            when (version) {
                "1.0" -> LexerFactory.createLexerV10(reader)
                "1.1" -> LexerFactory.createLexerV11(reader)
                else -> throw IllegalArgumentException("Unsupported version: $version")
            }

        // Crear parser según versión
        val parser =
            when (version) {
                "1.0" -> ParserFactory.createParserV10(lexer)
                "1.1" -> ParserFactory.createParserV11(lexer)
                else -> throw IllegalArgumentException("Unsupported version: $version")
            }

        // Crear output e input con los inputs del test
        val printer = HttpPrinter()
        val inputProvider = HttpInputProvider(LinkedList(inputs))

        // Crear interpreter según versión
        val interpreter =
            when (version) {
                "1.0" -> InterpreterFactory.createInterpreterVersion10(printer, inputProvider)
                "1.1" -> InterpreterFactory.createInterpreterVersion11(printer, inputProvider)
                else -> throw IllegalArgumentException("Unsupported version: $version")
            }

        // Ejecutar
        interpreter.interpret(parser)

        // Normalizamos saltos de línea al final
        return printer.prints.map { it.trimEnd('\n', '\r') }
    }

    /**
     * Compara outputs reales vs esperados.
     * Si no hay errores -> lista vacía -> el test PASA.
     * Si hay diferencias -> se agregan mensajes a la lista -> el test FALLA.
     */
    private fun compareOutputs(
        actualOutputsRaw: List<String>,
        expectedOutputs: List<String>,
    ): List<String> {
        // Normalizamos un poco: sacamos espacios y saltos de línea en bordes
        val actualOutputs = actualOutputsRaw.map { it.trim() }
        val normalizedExpected = expectedOutputs.map { it.trim() }

        val errors = mutableListOf<String>()

        // 1) Diferencia en cantidad de outputs
        if (actualOutputs.size != normalizedExpected.size) {
            errors += "Expected ${normalizedExpected.size} line(s) but got ${actualOutputs.size}"
        }

        // 2) Comparo elemento a elemento hasta el mínimo tamaño compartido
        val limit = min(actualOutputs.size, normalizedExpected.size)
        for (i in 0 until limit) {
            val expected = normalizedExpected[i]
            val actual = actualOutputs[i]

            if (expected != actual) {
                // Mensaje más claro: "Line 2: expected 'Result: 3' but got 'Result: 4'"
                errors += "Line ${i + 1}: expected '$expected' but got '$actual'"
            }
        }

        return errors
    }
}
