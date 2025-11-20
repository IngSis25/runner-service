package interpreter

import org.example.output.Output

class HttpPrinter : Output {
    val prints = mutableListOf<String>()

    override fun write(msg: String) {
        prints.add(msg)
    }
}
