package interpreter

import interfaces.InputProvider
import java.util.Queue

class HttpProvider(
    private val queue: Queue<String>,
) : InputProvider {
    override fun input(): String = queue.poll()
}
