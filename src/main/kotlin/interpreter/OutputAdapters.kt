package interpreter

import org.example.output.Output

class OutputAdapters : Output {
    override fun write(msg: String) = kotlin.io.print(msg)
}

class BufferOutput : Output {
    private val buffer = StringBuilder()

    override fun write(msg: String) {
        buffer.append(msg)
    }

    fun content(): String = buffer.toString()
}
