package com.rarible.protocol.currency.api.gecko

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.rarible.protocol.currency.core.gecko.FeignHelper
import com.rarible.protocol.currency.core.gecko.GeckoApi
import com.rarible.protocol.currency.core.gecko.GeckoApiImpl
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Tag
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.net.URI
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.stream.Stream

@Tag("manual")
class CoinGeckoApiTest {

    companion object {
        private val COIN_GECKO_ENDPOINT: URI = URI.create("https://api.coingecko.com/api/v3")

        @JvmStatic
        fun clients(): Stream<GeckoApi> {
            return Stream.of(
                FeignHelper.createClient<GeckoApi>(
                    ObjectMapper().registerKotlinModule(),
                    COIN_GECKO_ENDPOINT
                ),
                GeckoApiImpl(COIN_GECKO_ENDPOINT)
            )
        }
    }

    @ParameterizedTest
    @MethodSource("clients")
    fun testGeckoApiIntegration(client: GeckoApi) {
        val from = LocalDate.of(2022, 6, 4).atStartOfDay()
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
