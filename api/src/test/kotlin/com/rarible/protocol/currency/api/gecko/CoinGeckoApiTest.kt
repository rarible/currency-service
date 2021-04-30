package com.rarible.protocol.currency.api.gecko

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.rarible.protocol.currency.core.gecko.GeckoApi
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import ru.roborox.reactive.feign.FeignHelper
import java.time.LocalDate
import java.time.ZoneOffset

@Tag("manual")
class CoinGeckoApiTest {

    @Test
    fun testGeckoApiIntegration() {
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
        Assertions.assertTrue(eth.prices.isNotEmpty()) { "Ethereum prices must not be empty" }

        val dai = client
            .history("dai", from.toEpochSecond(ZoneOffset.UTC), to.toEpochSecond(ZoneOffset.UTC))
            .block()
        Assertions.assertTrue(dai.prices.isNotEmpty()) { "DAI prices must not be empty" }

        val weth = client
            .history("weth", from.toEpochSecond(ZoneOffset.UTC), to.toEpochSecond(ZoneOffset.UTC))
            .block()
        Assertions.assertTrue(weth.prices.isNotEmpty()) { "WETH prices must not be empty" }
    }
}