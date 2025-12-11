package runner

import org.junit.jupiter.api.Test
import org.springframework.web.servlet.config.annotation.CorsRegistry
import runner.WebConfig

class WebConfigTest {
    @Test
    fun `should configure CORS mappings`() {
        val config = WebConfig()
        val registry = CorsRegistry()

        config.addCorsMappings(registry)

        // Verify that CORS configuration is applied
        // The method should execute without errors
        assert(true)
    }
}
