package com.rarible.protocol.currency.api.metric

import io.micrometer.core.instrument.MeterRegistry
import org.springframework.stereotype.Component

@Component
class CurrencyJobMetrics(private val meterRegistry: MeterRegistry) {
    fun onCurrencyLoad(currencyId: String) {
        meterRegistry.counter(
            CURRENCY_LOAD,
            "currency_id", currencyId,
            "status", "ok",
        ).increment()
    }

    fun onCurrencyLoadNotFound(currencyId: String) {
        meterRegistry.counter(
            CURRENCY_LOAD,
            "currency_id", currencyId,
            "status", "fail",
            "reason", "not_found",
        ).increment()
    }

    fun onCurrencyLoadError(currencyId: String) {
        meterRegistry.counter(
            CURRENCY_LOAD,
            "currency_id", currencyId,
            "status", "fail",
            "reason", "unknown_error"
        ).increment()
    }

    companion object {
        const val CURRENCY_LOAD = "currency_load"
    }
}