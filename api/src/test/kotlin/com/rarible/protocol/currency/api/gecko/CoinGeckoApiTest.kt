package com.rarible.protocol.currency.api.gecko

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import ru.roborox.reactive.feign.FeignHelper
import java.time.LocalDate
import java.time.ZoneOffset


class CoinGeckoApiTest

fun main(args: Array<String>) {
    val objectMapper = ObjectMapper().registerKotlinModule()
    val client = FeignHelper.createClient<GeckoApi>(
        objectMapper,
        "https://api.coingecko.com/api/v3"
    )

    val from = LocalDate.of(2020, 1, 1).atStartOfDay()
    val to = from.plusDays(2)


    val eth = client
        .history("ethereum", from.toEpochSecond(ZoneOffset.UTC), to.toEpochSecond(ZoneOffset.UTC))
        .block()
    assert(eth.prices.isNotEmpty()) { "Ethereum prices must not be empty" }

    val dai = client
        .history("dai", from.toEpochSecond(ZoneOffset.UTC), to.toEpochSecond(ZoneOffset.UTC))
        .block()
    assert(dai.prices.isNotEmpty()) { "DAI prices must not be empty" }

    val weth = client
        .history("weth", from.toEpochSecond(ZoneOffset.UTC), to.toEpochSecond(ZoneOffset.UTC))
        .block()
    assert(weth.prices.isNotEmpty()) { "WETH prices must not be empty" }

}

fun assert(test: Boolean, message: () -> String) {
    if (!test) {
        throw RuntimeException(message())
    }
}