package com.rarible.protocol.currency.core.gecko

import com.rarible.protocol.currency.core.configuration.CurrencyApiProperties
import feign.FeignException
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.time.delay
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.web.reactive.function.client.WebClientResponseException
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference

class GeckoApiService(
    private val geckoApi: GeckoApi,
    properties: CurrencyApiProperties
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val request = properties.request

    suspend fun coinsList(): List<Coin> {
        return retry("coinsList") {
            geckoApi.coinsList().awaitSingle()
        } ?: emptyList()
    }

    suspend fun history(
        currencyId: String,
        from: Long,
        to: Long,
        vsCurrency: String = DEFAULT_VS_CURRENCY_PARAM_VALUR
    ): HistoryResponse? {
        return retry("history for $currencyId") {
            geckoApi.history(currencyId, from, to, vsCurrency).awaitSingle()
        }
    }

    private suspend fun <T> retry(method: String, call: suspend () -> T): T? {
        val attempt = AtomicInteger(1)
        val lastError = AtomicReference<Throwable>(null)
        while (attempt.getAndIncrement() <= request.attempts) {
            try {
                return call()
            } catch (ex: Throwable) {
                lastError.set(ex)
                when {
                    ex.isNotFound() -> return null
                    ex.canRetry().not() -> throw ex
                }
                logger.warn(
                    "Can't call '$method', attempt $attempt/${request.attempts}, cause=${ex.message ?: ex.cause?.message}"
                )
                delay(request.errorDelay)
            }
        }
        throw lastError.get()
    }

    private fun Throwable.isNotFound(): Boolean {
        return when (this) {
            is FeignException.NotFound -> true
            is WebClientResponseException -> {
                statusCode == HttpStatus.NOT_FOUND
            }
            else -> false
        }
    }

    private fun Throwable.canRetry(): Boolean {
        return when (this) {
            is FeignException.TooManyRequests,
            is FeignException.InternalServerError -> {
                true
            }
            is WebClientResponseException -> {
                statusCode == HttpStatus.TOO_MANY_REQUESTS || statusCode == HttpStatus.INTERNAL_SERVER_ERROR
            }
            else -> false
        }
    }
}