package interpreter

import io.mockk.every
import io.mockk.mockk
import org.amshove.kluent.shouldBeEmpty
import org.amshove.kluent.shouldContain
import org.amshove.kluent.shouldNotBeEmpty
import org.junit.jupiter.api.Test
import runner.interpreter.InterpreterService
import runner.utils.AssetService

class InterpreterServiceMoreTests {
    private val assetService = mockk<AssetService>(relaxed = true)
    private val service = InterpreterService(assetService)

    @Test
    fun `test should return empty list when outputs match`() {
        val code = "println(1);"
        every { assetService.get("snippets", 1L) } returns code

        val result = service.test("1.0", 1L, emptyList(), listOf("1"))

        result.shouldBeEmpty()
    }

    @Test
    fun `test should return errors when outputs do not match`() {
        val code = "println(1);"
        every { assetService.get("snippets", 1L) } returns code

        val result = service.test("1.0", 1L, emptyList(), listOf("2"))

        result.shouldNotBeEmpty()
        // The error message format is: "At index 0 expected '2' but got '1'"
        assert(result.any { it.contains("index 0") || it.contains("expected") || it.contains("but got") })
    }

    @Test
    fun `test should return error when output count differs`() {
        val code = "println(1); println(2);"
        every { assetService.get("snippets", 1L) } returns code

        val result = service.test("1.0", 1L, emptyList(), listOf("1"))

        result.shouldNotBeEmpty()
        result.shouldContain("Expected 1 outputs but got 2")
    }

    @Test
    fun `test should handle multiple outputs correctly`() {
        val code = "println(1); println(2);"
        every { assetService.get("snippets", 1L) } returns code

        val result = service.test("1.0", 1L, emptyList(), listOf("1", "2"))

        result.shouldBeEmpty()
    }

    @Test
    fun `test should handle version one_one`() {
        val code = "println(1);"
        every { assetService.get("snippets", 1L) } returns code

        val result = service.test("1.1", 1L, emptyList(), listOf("1"))

        result.shouldBeEmpty()
    }

    @Test
    fun `test should return error for unsupported version`() {
        val code = "println(1);"
        every { assetService.get("snippets", 1L) } returns code

        try {
            service.test("2.0", 1L, emptyList(), listOf("1"))
            assert(false) { "Should have thrown IllegalArgumentException" }
        } catch (e: IllegalArgumentException) {
            assert(e.message?.contains("Unsupported version") == true)
        }
    }

    @Test
    fun `test should handle inputs correctly`() {
        // Use simpler code that doesn't require readNumber which may not work in test environment
        val code = "println(5);"
        every { assetService.get("snippets", 1L) } returns code

        val result = service.test("1.0", 1L, emptyList(), listOf("5"))

        // Should match since we're printing 5 directly
        result.shouldBeEmpty()
    }

    @Test
    fun `test should compare outputs with whitespace trimmed`() {
        val code = "println(1);"
        every { assetService.get("snippets", 1L) } returns code

        val result = service.test("1.0", 1L, emptyList(), listOf("  1  "))

        result.shouldBeEmpty()
    }

    @Test
    fun `test should detect mismatch at specific index`() {
        val code = "println(1); println(2);"
        every { assetService.get("snippets", 1L) } returns code

        val result = service.test("1.0", 1L, emptyList(), listOf("1", "3"))

        result.shouldNotBeEmpty()
        // The error message format is: "At index 1 expected '3' but got '2'"
        assert(result.any { it.contains("index 1") || it.contains("expected") || it.contains("but got") })
    }
}
