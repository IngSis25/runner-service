package runner.app

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestTemplate
import runner.server.CorrelationIdInterceptor

@Configuration
class AppConfig(
    private val correlationIdInterceptor: CorrelationIdInterceptor,
) {
    @Bean
    fun restTemplate(): RestTemplate {
        val restTemplate = RestTemplate()
        restTemplate.interceptors.add(correlationIdInterceptor)
        return restTemplate
    }
}
