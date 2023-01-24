package com.rarible.protocol.currency.api.metric

import io.micrometer.core.instrument.MeterRegistry
import org.springframework.stereotype.Component

@Component
class CurrencyJobMetrics(private val meterRegistry: MeterRegistry) {
    fun onCurrencyLoad(currencyId: String) {
        meterRegistry.counter(
            CURRENCY_LOAD,
            "currency", currencyId
        ).increment()
    }

    fun onCurrencyLoadNotFound(currencyId: String) {
        meterRegistry.counter(
            CURRENCY_LOAD_ERROR,
            "currency", currencyId,
            "reason", "not_found",
        ).increment()
    }

    fun onCurrencyLoadError(currencyId: String) {
        meterRegistry.counter(
            CURRENCY_LOAD_ERROR,
            "currency", currencyId,
            "reason", "unknown_error"
        ).increment()
    }

    companion object {
        const val CURRENCY_LOAD = "currency_load"
        const val CURRENCY_LOAD_ERROR = "currency_load_error"
    }
}