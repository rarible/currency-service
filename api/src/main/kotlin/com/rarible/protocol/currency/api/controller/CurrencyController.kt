package com.rarible.protocol.currency.api.controller

import com.rarible.protocol.currency.api.service.CurrencyService
import com.rarible.protocol.currency.core.configuration.CurrencyApiProperties
import com.rarible.protocol.currency.core.converter.dto.RateDtoConverter
import com.rarible.protocol.currency.core.converter.model.BlockchainConverter
import com.rarible.protocol.currency.dto.BlockchainDto
import com.rarible.protocol.currency.dto.CurrencyRateDto
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController
import java.math.BigDecimal
import java.time.Instant

@RestController
class CurrencyController(
    private val currencyService: CurrencyService,
    private val currencyApiProperties: CurrencyApiProperties
) : CurrencyControllerApi {

    val logger: Logger = LoggerFactory.getLogger(CurrencyController::class.java)

    override suspend fun getCurrencyRate(
        blockchain: BlockchainDto,
        address: String,
        at: Long
    ): ResponseEntity<CurrencyRateDto> {
        val atDate = Instant.ofEpochMilli(at)
        logger.info("Get rate for [{}/{}] at {}", blockchain, address, atDate)

        val coinAlias = currencyApiProperties.byAddress(BlockchainConverter.convert(blockchain), address)
        if (coinAlias == null) {
            logger.warn(
                "Coin [{}/{}] is not supported. If this coin should be tracked, add it to application.yml.",
                blockchain, address
            )
            return ResponseEntity.ok().build()
        }

        val coinId = currencyApiProperties.getRealCoin(coinAlias)

        if (coinId == "usd") {
            return ResponseEntity.ok(
                CurrencyRateDto(
                    fromCurrencyId = coinAlias,
                    toCurrencyId = "usd",
                    rate = BigDecimal.ONE,
                    date = atDate
                )
            )
        }

        val geckoRate = currencyService.getRate(coinId, atDate)
        logger.info("Gecko response: {}", geckoRate)
        val result = geckoRate?.let {
            // In the response we need to specify original coin
            RateDtoConverter.convert(it).copy(fromCurrencyId = coinAlias)
        }
        return ResponseEntity.ok(result)
    }

}
