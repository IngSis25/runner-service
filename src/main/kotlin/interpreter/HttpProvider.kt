package interpreter

import java.util.Queue

class HttpProvider (private val queue: Queue<String>) : InputProvider {
    override fun input(): String {
        return queue.poll()
    }
}
interface InputProvider{
    fun input(): String
}
