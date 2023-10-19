package com.rarible.protocol.currency.api.controller

import com.rarible.protocol.currency.api.service.CurrencyService
import com.rarible.protocol.currency.core.configuration.CurrencyApiProperties
import com.rarible.protocol.currency.core.converter.dto.CurrencyDtoConverter
import com.rarible.protocol.currency.dto.CurrenciesDto
import com.rarible.protocol.currency.dto.CurrencyDto
import com.rarible.protocol.currency.dto.CurrencyRateDto
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController
import java.math.BigDecimal
import java.time.Instant

@RestController
class CurrencyController(
    private val currencyService: CurrencyService,
    private val currencyApiProperties: CurrencyApiProperties,
    private val converter: CurrencyDtoConverter
) : CurrencyControllerApi {

    val logger: Logger = LoggerFactory.getLogger(CurrencyController::class.java)

    override suspend fun getAllCurrencies(): ResponseEntity<CurrenciesDto> {
        val currencies = coroutineScope {
            currencyApiProperties.getAllCurrencies().map {
                async {
                    val rate = when (it.alias) {
                        "usd" -> BigDecimal.ONE
                        else -> currencyService.getRate(it.alias ?: it.currencyId, null)?.rate
                    }
                    CurrencyDto(
                        currencyId = it.currencyId,
                        alias = it.alias,
                        blockchain = it.blockchain,
                        address = it.address,
                        abbreviation = it.abbreviation,
                        rate = rate
                    )
                }
            }.awaitAll()
        }
        return ResponseEntity.ok(CurrenciesDto(currencies))
    }

    override suspend fun getCurrencyRate(
        blockchain: String,
        address: String,
        at: Long
    ): ResponseEntity<CurrencyRateDto> {
        val atDate = Instant.ofEpochMilli(at)
        logger.info("Get rate for [{}/{}] at {}", blockchain, address, atDate)

        val coinAlias = currencyApiProperties.byAddress(blockchain, address)
        if (coinAlias == null) {
            logger.warn(
                "Coin [{}/{}] is not supported. If this coin should be tracked, add it to application.yml.",
                blockchain, address
            )
            return ResponseEntity.ok().build()
        }

        val coinId = currencyApiProperties.getRealCoin(coinAlias)
        val abbreviation = currencyApiProperties.getAbbreviation(coinAlias)

        if (coinId == "usd") {
            return ResponseEntity.ok(
                CurrencyRateDto(
                    fromCurrencyId = coinAlias,
                    toCurrencyId = "usd",
                    rate = BigDecimal.ONE,
                    date = atDate,
                    abbreviation = abbreviation
                )
            )
        }

        val geckoRate = currencyService.getRate(coinId, atDate)
        logger.info("Gecko response: {}", geckoRate)
        val result = geckoRate?.let { converter.convert(it, coinAlias) }
        return ResponseEntity.ok(result)
    }
}
