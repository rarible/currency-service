package com.rarible.protocol.currency.api.service

import com.rarible.protocol.currency.core.model.Rate
import com.rarible.protocol.currency.core.repository.RateRepository
import org.bson.types.ObjectId
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.time.Instant

@Component
class CurrencyService(
    private val rateRepository: RateRepository
) {

    private val logger = LoggerFactory.getLogger(CurrencyService::class.java)

    suspend fun getRate(coinId: String, at: Instant?): Rate? {
        getHardcodedRate(coinId)?.let {
            return Rate(
                id = ObjectId(),
                currencyId = coinId,
                date = Instant.now(),
                rate = it,
                version = null
            )
        }
        logger.info("Coin id = {}", coinId)
        return at?.let { rateRepository.getRate(coinId, at) } ?: rateRepository.findLast(coinId)
    }

    // TODO !!!
    // There is no rates for these currencies at CoinGecko, we need to fetch them from somewhere else
    private fun getHardcodedRate(coinId: String): BigDecimal? {
        return when (coinId) {
            "lightlink" -> BigDecimal("0.2811")
            else -> null
        }
    }
}
