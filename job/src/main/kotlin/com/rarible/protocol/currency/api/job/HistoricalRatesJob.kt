package com.rarible.protocol.currency.api.job

import com.rarible.protocol.currency.api.gecko.GeckoApi
import com.rarible.protocol.currency.core.gecko.HistoryResponse
import com.rarible.protocol.currency.core.configuration.CurrencyApiProperties
import com.rarible.protocol.currency.core.model.Rate
import com.rarible.protocol.currency.core.repository.RateRepository
import kotlinx.coroutines.reactive.awaitFirstOrDefault
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.temporal.ChronoUnit

@Component
class HistoricalRatesJob(
    val properties: CurrencyApiProperties,
    val rateRepository: RateRepository,
    val geckoApi: GeckoApi
) {

    @Scheduled(initialDelay = 60000, fixedDelay = 3600000)
    fun loadPriceHistory(): Unit = runBlocking {
        logger.info("Starting load of historical prices")
        properties.coins.forEach {
            loadCurrency(it.key)
        }
        logger.info("Finished load of historical prices")
    }

    suspend fun loadCurrency(currencyId: String) {
        val last = rateRepository.findLast(currencyId)
        val from = if(last == null) {
            val since = properties.historySince.toInstant()
            logger.info("No history for {}. Loading from historySince={}", currencyId, since.toString())
            since
        } else {
            logger.info("Last entry for {} is {}. Proceeding loading.", currencyId, last)
            last.date.toInstant()
        }


        val rates = loadCurrency(currencyId, from.epochSecond, from.plus(90, ChronoUnit.DAYS).epochSecond)
        logger.info("Received {} rates for {}", rates.size, currencyId)
        rateRepository.saveAll(rates)
    }

    suspend fun loadCurrency(currencyId: String, from: Long, to: Long): List<Rate> {
        return geckoApi
            .history(currencyId, from, to)
            .awaitFirstOrDefault(HistoryResponse())
            .prices
            .map { (date, rate) ->
                Rate.of(currencyId, date, rate)
            }
    }

    companion object {
        val logger = LoggerFactory.getLogger(HistoricalRatesJob::class.java)
    }
}