package com.rarible.protocol.currency.api.converter

import com.rarible.protocol.currency.core.model.Rate
import com.rarible.protocol.dto.CurrencyRateDto
import org.springframework.core.convert.converter.Converter
import org.springframework.stereotype.Component

@Component
object RateDtoConverter : Converter<Rate, CurrencyRateDto> {
    override fun convert(source: Rate): CurrencyRateDto {
        return CurrencyRateDto(
            fromCurrencyId = source.currencyId,
            toCurrencyId = "usd",
            rate = source.rate,
            date = source.date
        )
    }
}


