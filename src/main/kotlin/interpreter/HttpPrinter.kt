package interpreter

import interfaces.Printer

class HttpPrinter : Printer {
    val prints = mutableListOf<String>()

    override fun print(message: String) {
        prints.add(message)
    }
}
