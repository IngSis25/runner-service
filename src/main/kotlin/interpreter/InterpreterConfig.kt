package interpreter

import org.example.strategy.PreConfiguredProviders
import org.example.strategy.StrategyProvider
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class InterpreterConfig {
    @Bean(name = ["ProviderV10"])
    fun providerV10(): StrategyProvider = PreConfiguredProviders.VERSION_1_0

    @Bean(name = ["ProviderV11"])
    fun providerV11(): StrategyProvider = PreConfiguredProviders.VERSION_1_1

    @Bean
    fun providerFactory(
        @Qualifier("ProviderV10") providerV10: StrategyProvider,
        @Qualifier("ProviderV11") providerV11: StrategyProvider,
    ): (String) -> StrategyProvider =
        { version ->
            when (version) {
                "1.0" -> providerV10
                "1.1" -> providerV11
                else -> error("Unsupported PrintScript version: $version")
            }
        }
}
