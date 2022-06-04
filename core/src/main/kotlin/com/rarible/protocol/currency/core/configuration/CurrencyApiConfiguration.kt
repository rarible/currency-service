package com.rarible.protocol.currency.core.configuration

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.rarible.protocol.currency.core.gecko.FeignHelper
import com.rarible.protocol.currency.core.gecko.GeckoApi
import com.rarible.protocol.currency.core.gecko.GeckoApiImpl
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(CurrencyApiProperties::class)
class CurrencyApiConfiguration(
    val properties: CurrencyApiProperties
) {

    @Bean
    fun objectMapper(): ObjectMapper {
        return ObjectMapper().registerModule(KotlinModule())
    }

    @Bean
    fun geckoApi(objectMapper: ObjectMapper): GeckoApi {
        return when (properties.clientType) {
            ClientType.FEIGN -> {
                FeignHelper.createClient(objectMapper, baseUrl = properties.apiUrl, proxyUrl = properties.proxyUrl)
            }
            ClientType.WEB -> {
                GeckoApiImpl(baseUrl = properties.apiUrl, proxyUrl = properties.proxyUrl)
            }
        }
    }
}
