package com.rarible.protocol.currency.api.job

import com.rarible.protocol.currency.api.metric.CurrencyJobMetrics
import com.rarible.protocol.currency.core.configuration.CurrencyApiProperties
import com.rarible.protocol.currency.core.gecko.GeckoApiService
import com.rarible.protocol.currency.core.gecko.HistoryResponse
import com.rarible.protocol.currency.core.model.Rate
import com.rarible.protocol.currency.core.repository.RateRepository
import feign.FeignException
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.time.delay
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.concurrent.atomic.AtomicReference

@Component
class HistoricalRatesJob(
    private val properties: CurrencyApiProperties,
    private val rateRepository: RateRepository,
    private val geckoApi: GeckoApiService,
    private val currencyJobMetrics: CurrencyJobMetrics,
) {
    private val request = properties.request
    private val geckoCoinIds = AtomicReference<Set<String>>(emptySet())

    private val dedicatedCoins = listOf(
        "seur" // EUR stable coin
    )

    @Scheduled(initialDelay = 60000, fixedDelay = 3600000)
    fun loadPriceHistory(): Unit = runBlocking {
        val existedCoinIds = getExistedCoinIds()
        val coins = (properties.coins.map { it.key } + dedicatedCoins)
            .filter { currencyId -> currencyId in existedCoinIds }

        logger.info("Starting load of historical prices for {} coins", coins.size)

        coins.forEach { currencyId ->
            var attempt = 1

            while (attempt <= request.attempts) {
                try {
                    loadCurrency(currencyId)
                    currencyJobMetrics.onCurrencyLoad(currencyId)
                    break
                } catch (ex: FeignException.NotFound) {
                    logger.warn(
                        "Currency $currencyId not found, cause=${ex.message ?: ex.cause?.message}"
                    )
                    currencyJobMetrics.onCurrencyLoadNotFound(currencyId)
                    break
                } catch (ex: Throwable) {
                    logError(attempt, currencyId, ex)
                    currencyJobMetrics.onCurrencyLoadError(currencyId)
                    delay(request.errorDelay)
                }
                attempt += 1
            }
            delay(request.delay)
        }
        logger.info("Finished load of historical prices")
    }

    private fun logError(attempt: Int, currencyId: String, ex: Throwable) {
        val reason = ex.message ?: ex.cause?.message
        if (attempt == request.attempts) {
            logger.error("couldn't load rates for $currencyId after $attempt attempts, cause=${reason}", ex)
        } else {
            logger.info("couldn't load rates for $currencyId, attempt $attempt/${request.attempts}, cause=${reason}")
        }
    }

    suspend fun loadCurrency(currencyId: String) {
        val last = rateRepository.findLast(currencyId)
        val from = if (last == null) {
            val since = properties.historySince
            logger.info("No history for {}. Loading from historySince={}", currencyId, since.toString())
            since
        } else {
            val lastPlusHour = last.date.plus(1, ChronoUnit.HOURS)
            logger.info("Last entry for {} is {}. Proceeding loading starting with {}", currencyId, last, lastPlusHour)
            lastPlusHour
        }
        val rates = loadCurrency(currencyId, from, Instant.now())
        if (rates.isNotEmpty()) {
            rateRepository.saveAll(rates)
            loadCurrency(currencyId)
        }
    }

    suspend fun loadCurrency(currencyId: String, from: Instant, maxTo: Instant): List<Rate> {
        if (from.isAfter(maxTo)) {
            logger.warn("No rates found for {}", currencyId)
            return emptyList()
        } else {
            val to = from.plus(90, ChronoUnit.DAYS)
            val rates = (geckoApi.history(currencyId, from.epochSecond, to.epochSecond) ?: HistoryResponse())
                .prices
                .map { (date, rate) ->
                    Rate.of(currencyId, date, rate)
                }

            return if (rates.isEmpty()) {
                logger.info("No rates found for {} in range {} - {}. Trying next range...", currencyId, from, to)
                delay(request.delay)
                loadCurrency(currencyId, to, maxTo)
            } else {
                logger.info("Received {} rates for {} in range {} - {}", rates.size, currencyId, from, to)
                rates
            }
        }
    }

    private suspend fun getExistedCoinIds(): Set<String> {
        val existedCoinIds = geckoCoinIds.get()
        return existedCoinIds.ifEmpty {
            val geckoCoins = geckoApi.coinsList()
            val fetchedCoinIds = geckoCoins.map { it.id }.toSet()
            geckoCoinIds.set(fetchedCoinIds)
            fetchedCoinIds
        }
    }

    companion object {
        val logger: Logger = LoggerFactory.getLogger(HistoricalRatesJob::class.java)
    }
}
