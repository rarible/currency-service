package com.rarible.protocol.currency.api.job

import com.rarible.protocol.currency.core.configuration.CurrencyApiProperties
import com.rarible.protocol.currency.core.gecko.GeckoApi
import com.rarible.protocol.currency.core.gecko.HistoryResponse
import com.rarible.protocol.currency.core.model.Rate
import com.rarible.protocol.currency.core.repository.RateRepository
import kotlinx.coroutines.reactive.awaitFirstOrDefault
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.time.delay
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.Instant
import java.time.temporal.ChronoUnit

@Component
class HistoricalRatesJob(
    private val properties: CurrencyApiProperties,
    private val rateRepository: RateRepository,
    private val geckoApi: GeckoApi
) {
    private val request = properties.request

    private val dedicatedCoins = listOf(
        "seur" // EUR stable coin
    )

    @Scheduled(initialDelay = 60000, fixedDelay = 3600000)
    fun loadPriceHistory(): Unit = runBlocking {
        val coins = properties.coins.map { it.key } + dedicatedCoins
        logger.info("Starting load of historical prices for {} coins", coins.size)

        coins.forEach { currencyId ->
            var attempt = 1

            while (attempt <= request.attempts) {
                try {
                    loadCurrency(currencyId)
                    break
                } catch (ex: Throwable) {
                    logger.error("Can't load currency for $currencyId, attempt $attempt/${request.attempts}", ex)
                    delay(request.errorDelay)
                }
                attempt += 1
            }
            delay(request.delay)
        }
        logger.info("Finished load of historical prices")
    }

    suspend fun loadCurrency(currencyId: String) {
        val last = rateRepository.findLast(currencyId)
        val from = if(last == null) {
            val since = properties.historySince
            logger.info("No history for {}. Loading from historySince={}", currencyId, since.toString())
            since
        } else {
            val lastPlusHour = last.date.plus(1, ChronoUnit.HOURS)
            logger.info("Last entry for {} is {}. Proceeding loading starting with {}", currencyId, last, lastPlusHour)
            lastPlusHour
        }

        val rates = loadCurrency(currencyId, from, Instant.now())
        if(rates.isNotEmpty()) {
            rateRepository.saveAll(rates)
            loadCurrency(currencyId)
        }
    }

    suspend fun loadCurrency(currencyId: String, from: Instant, maxTo: Instant): List<Rate> {
        if(from.isAfter(maxTo)) {
            logger.warn("No rates found for {}", currencyId)
            return emptyList()
        } else {
            val to = from.plus(90, ChronoUnit.DAYS)
            val rates = geckoApi
                .history(currencyId, from.epochSecond, to.epochSecond)
                .awaitFirstOrDefault(HistoryResponse())
                .prices
                .map { (date, rate) ->
                    Rate.of(currencyId, date, rate)
                }

            return if (rates.isEmpty()) {
                logger.info("No rates found for {} in range {} - {}. Trying next range...", currencyId, from, to)
                loadCurrency(currencyId, to, maxTo)
            } else {
                logger.info("Received {} rates for {} in range {} - {}", rates.size, currencyId, from, to)
                rates
            }
        }
    }

    companion object {
        val logger: Logger = LoggerFactory.getLogger(HistoricalRatesJob::class.java)
    }
}
