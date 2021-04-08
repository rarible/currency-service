package com.rarible.protocol.currency.api.configuration

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.rarible.protocol.currency.api.gecko.GeckoApi
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ru.roborox.reactive.feign.FeignHelper
import ru.roborox.reactive.persist.configuration.EnableRoboroxMongo

@Configuration
@EnableRoboroxMongo
class CurrencyApiConfiguration(
    val properties: CurrencyApiProperties
) {

    @Bean
    fun objectMapper() = ObjectMapper()
        .registerModule(KotlinModule())
        .registerModule(JavaTimeModule())

    @Bean
    fun geckoApi(
        objectMapper: ObjectMapper
    ): GeckoApi =
        FeignHelper.createClient(objectMapper, properties.apiUrl)
}