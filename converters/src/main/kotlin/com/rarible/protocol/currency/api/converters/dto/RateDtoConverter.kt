package com.rarible.protocol.currency.api.converters.dto

import com.rarible.protocol.currency.api.dto.RateDto
import com.rarible.protocol.currency.core.model.Rate
import org.springframework.core.convert.converter.Converter
import org.springframework.stereotype.Component

@Component
object RateDtoConverter : Converter<Rate, RateDto> {
    override fun convert(source: Rate): RateDto {
        return RateDto(
            fromCurrencyId = source.currencyId,
            toCurrencyId = "usd",
            rate = source.rate,
            date = source.date
        )
    }
}


