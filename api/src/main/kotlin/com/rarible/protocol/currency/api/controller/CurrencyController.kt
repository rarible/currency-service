package com.rarible.protocol.currency.api.controller

import com.rarible.core.common.conversion.convert
import com.rarible.core.common.coroutine.coroutineToMono
import com.rarible.core.model.type.Blockchain
import com.rarible.protocol.currency.api.CurrencyApi
import com.rarible.protocol.currency.core.configuration.CurrencyApiProperties
import com.rarible.protocol.currency.api.dto.RateDto
import com.rarible.protocol.currency.core.repository.RateRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.core.convert.ConversionService
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono
import scalether.domain.Address
import java.util.*

@RestController
@RequestMapping(
    value = ["/v0.1/currency"],
    produces = [MediaType.APPLICATION_JSON_VALUE]
)
class CurrencyController(
    private val rateRepository: RateRepository,
    private val conversionService: ConversionService,
    private val currencyApiProperties: CurrencyApiProperties
) : CurrencyApi {

    val logger: Logger = LoggerFactory.getLogger(CurrencyController::class.java)

    override fun getRate(blockchain: Blockchain, address: Address, at: Date): Mono<RateDto> {
        logger.info("Get rate for [{}/{}] at {}", blockchain, address, at)
        val coinId = currencyApiProperties.byAddress(blockchain, address)

        return coroutineToMono {
            if(coinId == null) {
                logger.warn(
                    "Coin [{}/{}] is not supported. If this coin should be tracked, add it to application.yml.",
                    blockchain,
                    address
                )
                null
            } else {
                logger.info("Coin id = {}", coinId)
                val geckoRate = rateRepository.getRate(coinId, at)
                logger.info("Gecko response: {}", geckoRate)
                geckoRate?.let {
                    conversionService.convert<RateDto>(it)
                }
            }
        }
    }
}