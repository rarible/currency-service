package com.rarible.protocol.currency.core.configuration

import com.rarible.protocol.currency.core.converter.dto.CurrencyDtoConverter
import com.rarible.protocol.currency.core.repository.RateRepository
import org.springframework.context.annotation.Bean
import org.springframework.data.mongodb.core.ReactiveMongoTemplate

class RepositoryConfiguration(
    private val template: ReactiveMongoTemplate,
    private val currencyApiProperties: CurrencyApiProperties
) {

    @Bean
    fun rateRepository(): RateRepository {
        return RateRepository(template)
    }

    @Bean
    fun currencyDtoConverter(): CurrencyDtoConverter {
        return CurrencyDtoConverter(currencyApiProperties)
    }
}