package config

import org.junit.jupiter.api.Test
import runner.config.ConnectionFactory

class ConnectionFactoryTest {
    @Test
    fun `should create redis connection factory`() {
        val factory = ConnectionFactory("localhost", 6379)

        val connectionFactory = factory.redisConnectionFactory()

        assert(connectionFactory != null)
        assert(connectionFactory.hostName == "localhost")
        assert(connectionFactory.port == 6379)
    }

    @Test
    fun `should create redis connection factory with custom host and port`() {
        val factory = ConnectionFactory("redis.example.com", 6380)

        val connectionFactory = factory.redisConnectionFactory()

        assert(connectionFactory != null)
        assert(connectionFactory.hostName == "redis.example.com")
        assert(connectionFactory.port == 6380)
    }
}
