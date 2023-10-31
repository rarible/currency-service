package com.rarible.protocol.currency.core.gecko

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import reactor.core.publisher.Mono

internal const val ID_PARAM = "id"
internal const val FROM_PARAM = "from"
internal const val TO_PARAM = "to"
internal const val VS_CURRENCIES_PARAM = "vs_currencies"
internal const val VS_CURRENCY_PARAM = "vs_currency"
internal const val DEFAULT_VS_CURRENCY_PARAM_VALUR = "usd"

internal const val CURRENT_PRICE_PATH = "/simple/price"
internal const val HISTORY_PATH = "/coins/{$ID_PARAM}/market_chart/range"
internal const val COINS_LIST_PATH = "/coins/list"

interface GeckoApi {

    @GetMapping(CURRENT_PRICE_PATH)
    fun currentPrice(
        @RequestParam(ID_PARAM)
        coinId: String,

        @RequestParam(value = VS_CURRENCIES_PARAM, required = false, defaultValue = DEFAULT_VS_CURRENCY_PARAM_VALUR)
        vsCurrency: String
    ): Mono<Map<String, Map<String, Double>>>

    @GetMapping(COINS_LIST_PATH)
    fun coinsList(): Mono<List<Coin>>

    @GetMapping(HISTORY_PATH)
    fun history(
        @PathVariable(ID_PARAM)
        currencyId: String,

        @RequestParam(FROM_PARAM)
        from: Long,

        @RequestParam(TO_PARAM)
        to: Long,

        @RequestParam(value = VS_CURRENCY_PARAM, required = false, defaultValue = DEFAULT_VS_CURRENCY_PARAM_VALUR)
        vsCurrency: String = DEFAULT_VS_CURRENCY_PARAM_VALUR
    ): Mono<HistoryResponse>
}
