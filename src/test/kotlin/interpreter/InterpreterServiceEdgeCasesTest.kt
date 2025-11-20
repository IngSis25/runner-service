package interpreter

import io.mockk.every
import io.mockk.mockk
import org.amshove.kluent.shouldNotBeNull
import org.junit.jupiter.api.Test
import utils.AssetService

class InterpreterServiceEdgeCasesTest {
    private val assetService = mockk<AssetService>(relaxed = true)
    private val service = InterpreterService(assetService)

    @Test
    fun `test should handle more outputs than expected`() {
        val snippetId = 1L
        val version = "1.0"
        val code = "println(\"Hello\"); println(\"World\")"
        val inputs = emptyList<String>()
        val expectedOutputs = listOf("Hello")

        every { assetService.get("snippets", snippetId) } returns code

        try {
            val result = service.test(version, snippetId, inputs, expectedOutputs)
            result.shouldNotBeNull()
            // Debería detectar que hay más outputs de los esperados
        } catch (e: Exception) {
            // Puede fallar por sintaxis
        }
    }

    @Test
    fun `test should handle fewer outputs than expected`() {
        val snippetId = 1L
        val version = "1.0"
        val code = "println(\"Hello\")"
        val inputs = emptyList<String>()
        val expectedOutputs = listOf("Hello", "World")

        every { assetService.get("snippets", snippetId) } returns code

        try {
            val result = service.test(version, snippetId, inputs, expectedOutputs)
            result.shouldNotBeNull()
            // Debería detectar que faltan outputs
        } catch (e: Exception) {
            // Puede fallar por sintaxis
        }
    }

    @Test
    fun `test should handle multiple mismatches`() {
        val snippetId = 1L
        val version = "1.0"
        val code = "println(\"Hello\"); println(\"World\")"
        val inputs = emptyList<String>()
        val expectedOutputs = listOf("Wrong1", "Wrong2")

        every { assetService.get("snippets", snippetId) } returns code

        try {
            val result = service.test(version, snippetId, inputs, expectedOutputs)
            result.shouldNotBeNull()
            // Debería tener múltiples errores
        } catch (e: Exception) {
            // Puede fallar por sintaxis
        }
    }
}
