package com.rarible.protocol.currency.core.gecko

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import reactor.core.publisher.Mono


interface GeckoApi {

    @GetMapping("/simple/price")
    fun currentPrice(
        @RequestParam("id")
        coinId: String,

        @RequestParam(value = "vs_currencies", required = false, defaultValue = "usd")
        vsCurrency: String
    ): Mono<Map<String, Map<String, Double>>>

    @GetMapping("/coins/{id}/market_chart/range")
    fun history(
        @PathVariable("id")
        currencyId: String,

        @RequestParam("from")
        from: Long,

        @RequestParam("to")
        to: Long,

        @RequestParam(value = "vs_currency", required = false, defaultValue = "usd")
        vsCurrency: String = "usd"
    ): Mono<HistoryResponse>
}