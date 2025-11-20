package interpreter

import org.example.input.Input
import java.util.Queue

class HttpInputProvider(
    private val queue: Queue<String>,
) : Input {
    override fun read(message: String): String = queue.poll() ?: ""
}
