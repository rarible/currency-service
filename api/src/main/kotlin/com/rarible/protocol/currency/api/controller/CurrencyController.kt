package com.rarible.protocol.currency.api.controller

import com.rarible.core.common.conversion.convert
import com.rarible.core.common.coroutine.coroutineToMono
import com.rarible.protocol.currency.api.CurrencyApi
import com.rarible.protocol.currency.api.dto.RateDto
import com.rarible.protocol.currency.core.repository.RateRepository
import org.springframework.core.convert.ConversionService
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono
import java.util.*

@RestController
class CurrencyController(
    private val rateRepository: RateRepository,
    private val conversionService: ConversionService
): CurrencyApi {
    override fun getRate(currencyId: String, date: Date): Mono<RateDto> = coroutineToMono {
        rateRepository.getRate(currencyId, date)?.let {
            conversionService.convert<RateDto>(it)
        }
    }
}