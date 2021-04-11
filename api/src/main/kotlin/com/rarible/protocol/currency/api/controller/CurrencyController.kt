package com.rarible.protocol.currency.api.controller

import com.rarible.core.common.conversion.convert
import com.rarible.core.common.coroutine.coroutineToMono
import com.rarible.protocol.currency.api.CurrencyApi
import com.rarible.protocol.currency.api.configuration.CurrencyApiProperties
import com.rarible.protocol.currency.api.configuration.Platform
import com.rarible.protocol.currency.api.dto.RateDto
import com.rarible.protocol.currency.core.repository.RateRepository
import org.springframework.core.convert.ConversionService
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono
import scalether.domain.Address
import java.util.*

@RestController("/v0.1/currency")
class CurrencyController(
    private val rateRepository: RateRepository,
    private val conversionService: ConversionService,
    private val currencyApiProperties: CurrencyApiProperties
): CurrencyApi {

    override fun getRate(blockchain: String, address: Address, at: Date): Mono<RateDto> = coroutineToMono {
        currencyApiProperties
            .byAddress(Platform.of(blockchain), address)
            ?.let {
                rateRepository.getRate(it, at)
            }
            ?.let {
                conversionService.convert<RateDto>(it)
            }
    }
}