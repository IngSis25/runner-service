package runner.interpreter

import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/printscript")
class InterpreterController(
    private val interpreterService: InterpreterService,
) {
    /**
     * Interpret the given code and return a list with all the outputs
     * @param interpretDto the dto with the version and code to interpret
     * @return a list with all the outputs
     */
    @PostMapping("/interpret")
    fun interpret(
        @RequestBody interpretDto: InterpretDto,
    ): List<String> {
        val version = interpretDto.version
        val code = interpretDto.code

        return interpreterService.interpret(version, code)
    }

    /**
     * Test the given code with the given inputs and return a list with all the results
     * @param testDto the dto with the version, code, inputs and outputs to test
     * @return a list with all the errors. If there are no errors, the list will be empty
     */
    @PostMapping("/test")
    fun test(
        @RequestBody testDto: TestDto,
    ): List<String> {
        val version = testDto.version
        val snippetId = testDto.snippetId
        val inputs = testDto.inputs
        val outputs = testDto.outputs

        return interpreterService.test(version, snippetId, inputs, outputs)
    }
}
