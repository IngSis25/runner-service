package interpreter

import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldContain
import org.junit.jupiter.api.Test
import runner.interpreter.HttpPrinter

class HttpPrinterTest {
    @Test
    fun `should collect prints in list`() {
        val printer = HttpPrinter()

        printer.write("message1")
        printer.write("message2")

        printer.prints.size shouldBeEqualTo 2
        printer.prints shouldContain "message1"
        printer.prints shouldContain "message2"
    }

    @Test
    fun `should handle empty message`() {
        val printer = HttpPrinter()

        printer.write("")

        printer.prints.size shouldBeEqualTo 1
        printer.prints[0] shouldBeEqualTo ""
    }

    @Test
    fun `should handle multiple writes`() {
        val printer = HttpPrinter()

        for (i in 1..10) {
            printer.write("message$i")
        }

        printer.prints.size shouldBeEqualTo 10
    }
}
