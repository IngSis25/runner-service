package interpreter

import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test
import runner.interpreter.HttpInputProvider
import java.util.LinkedList
import java.util.Queue

class HttpInputProviderTest {
    @Test
    fun `should read from queue when available`() {
        val queue: Queue<String> = LinkedList()
        queue.add("input1")
        queue.add("input2")
        val provider = HttpInputProvider(queue)

        val result1 = provider.read("prompt")
        val result2 = provider.read("prompt")

        result1 shouldBeEqualTo "input1"
        result2 shouldBeEqualTo "input2"
    }

    @Test
    fun `should return empty string when queue is empty`() {
        val queue: Queue<String> = LinkedList()
        val provider = HttpInputProvider(queue)

        val result = provider.read("prompt")

        result shouldBeEqualTo ""
    }

    @Test
    fun `should return empty string after queue is exhausted`() {
        val queue: Queue<String> = LinkedList()
        queue.add("input1")
        val provider = HttpInputProvider(queue)

        provider.read("prompt")
        val result = provider.read("prompt")

        result shouldBeEqualTo ""
    }
}
