package com.rarible.protocol.currency.api.service

import com.rarible.protocol.currency.core.model.Rate
import com.rarible.protocol.currency.core.repository.RateRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class CurrencyService(
    private val rateRepository: RateRepository
) {

    private val logger = LoggerFactory.getLogger(CurrencyService::class.java)

    suspend fun getRate(coinId: String, at: Instant?): Rate? {
        logger.info("Coin id = {}", coinId)
        return at?.let { rateRepository.getRate(coinId, at) } ?: rateRepository.findLast(coinId)
    }
}
