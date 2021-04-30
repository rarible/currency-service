package com.rarible.protocol.currency.core.configuration

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.rarible.protocol.currency.core.gecko.GeckoApi
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ru.roborox.reactive.feign.FeignHelper

@Configuration
@EnableConfigurationProperties(CurrencyApiProperties::class)
class CurrencyApiConfiguration(
    val properties: CurrencyApiProperties
) {

    @Bean
    fun objectMapper() = ObjectMapper()
        .registerModule(KotlinModule())

    @Bean
    fun geckoApi(
        objectMapper: ObjectMapper
    ): GeckoApi =
        FeignHelper.createClient(objectMapper, properties.apiUrl)
}