package com.rarible.protocol.currency.core.converter.dto

import com.rarible.protocol.currency.core.configuration.CurrencyApiProperties
import com.rarible.protocol.currency.core.model.Rate
import com.rarible.protocol.currency.dto.CurrencyRateDto

class CurrencyDtoConverter(
    private val currencyApiProperties: CurrencyApiProperties
) {

    fun convert(source: Rate, alias: String? = null): CurrencyRateDto {
        val coinAlias = alias ?: source.currencyId
        return CurrencyRateDto(
            fromCurrencyId = coinAlias,
            toCurrencyId = "usd",
            rate = source.rate,
            date = source.date,
            abbreviation = currencyApiProperties.getAbbreviation(coinAlias)
        )
    }
}


