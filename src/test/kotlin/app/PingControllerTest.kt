package app

import org.amshove.kluent.shouldEqual
import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders

class PingControllerTest {
    private val controller = PingController()
    private val mockMvc = MockMvcBuilders.standaloneSetup(controller).build()

    @Test
    fun `ping should return pong`() {
        val result = controller.ping()

        result shouldEqual "pong"
    }

    @Test
    fun `ping endpoint should return 200 OK`() {
        mockMvc
            .perform(get("/ping"))
            .andExpect(status().isOk)
            .andExpect(content().string("pong"))
    }
}
