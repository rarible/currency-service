package com.rarible.protocol.currency.api.metric

import io.micrometer.core.instrument.MeterRegistry
import org.springframework.stereotype.Component

@Component
class CurrencyJobMetrics(private val meterRegistry: MeterRegistry) {
    fun onCurrencyLoadFail(currencyId: String) {
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