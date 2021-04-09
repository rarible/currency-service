package com.rarible.protocol.currency.api.job

import com.rarible.protocol.currency.api.configuration.CurrencyApiProperties
import com.rarible.protocol.currency.api.gecko.GeckoApi
import com.rarible.protocol.currency.api.gecko.HistoryResponse
import com.rarible.protocol.currency.core.model.Rate
import com.rarible.protocol.currency.core.repository.RateRepository
import kotlinx.coroutines.reactive.awaitFirstOrDefault
import kotlinx.coroutines.runBlocking
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
        properties.coins.forEach {
            loadCurrency(it.key)
        }
    }

    suspend fun loadCurrency(currencyId: String) {
        val last = rateRepository.findLast(currencyId)
        val from = if(last == null) {
            properties.historySince.toInstant()
        } else {
            last.date.toInstant()
        }

        val rates = loadCurrency(currencyId, from.epochSecond, from.plus(90, ChronoUnit.DAYS).epochSecond)
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
}