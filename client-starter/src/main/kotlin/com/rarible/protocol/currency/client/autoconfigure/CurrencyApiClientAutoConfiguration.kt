package com.rarible.protocol.gateway.client.autoconfigure

import com.rarible.core.application.ApplicationEnvironmentInfo
import com.rarible.protocol.order.api.client.CurrencyApiClientFactory
import com.rarible.protocol.order.api.client.CurrencyApiServiceUriProvider
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean

class CurrencyApiClientAutoConfiguration(
    private val applicationEnvironmentInfo: ApplicationEnvironmentInfo
) {
    @Bean
    @ConditionalOnMissingBean(CurrencyApiServiceUriProvider::class)
    fun currencyApiServiceUriProvider(): CurrencyApiServiceUriProvider {
        return CurrencyApiServiceUriProvider(applicationEnvironmentInfo.name)
    }

    @Bean
    @ConditionalOnMissingBean(CurrencyApiClientFactory::class)
    fun currencyApiClientFactory(currencyApiServiceUriProvider: CurrencyApiServiceUriProvider): CurrencyApiClientFactory {
        return CurrencyApiClientFactory(currencyApiServiceUriProvider)
    }
}