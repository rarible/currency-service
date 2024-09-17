package com.rarible.protocol.currency.api.configuration

import com.rarible.protocol.currency.core.configuration.CurrencyApiProperties
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import scalether.domain.Address
import java.net.URI
import java.time.Instant

class CurrencyApiPropertiesTest {

    @Test
    fun `should get correct coin id by address`() {
        val props = CurrencyApiProperties(
            URI.create("localhost"),
            mapOf(
                "ethereum" to mapOf("ETHEREUM" to listOf("0x0000000000000000000000000000000000000000")),
                "dai" to mapOf("ETHEREUM" to listOf(DAI))
            ),
            mapOf(),
            18,
            mapOf(),
            Instant.now()
        )

        Assertions.assertEquals("ethereum", props.byAddress("ETHEREUM", Address.ZERO().prefixed()))
        Assertions.assertEquals("dai", props.byAddress("ETHEREUM", Address.apply(DAI).prefixed()))
    }

    @Test
    fun `should get alias by coin id`() {
        val props = CurrencyApiProperties(
            URI.create("localhost"),
            mapOf(
                "flowusd" to mapOf("FLOW" to listOf("123")),
                "flow" to mapOf("FLOW" to listOf("321")),
            ),
            mapOf(),
            18,
            mapOf("flowusd" to "usd"),
            Instant.now(),

        )

        val flowUsdAlias = props.byAddress("FLOW", "123")!!
        val flow = props.byAddress("FLOW", "321")!!

        assertThat(flowUsdAlias).isEqualTo("flowusd")
        assertThat(flow).isEqualTo("flow")

        // Aliased coin
        assertThat(props.getRealCoin(flowUsdAlias)).isEqualTo("usd")
        // Real coin
        assertThat(props.getRealCoin(flow)).isEqualTo("flow")
    }

    @Test
    fun `should populate currencies decimals`() {
        val props = CurrencyApiProperties(
            URI.create("localhost"),
            mapOf(
                "degen-base" to mapOf("BASE" to listOf("0x0000000000000000000000000000000000000000")),
                "dai" to mapOf("PALM" to listOf(DAI))
            ),
            mapOf(
                "degen-base" to mapOf("BASE" to 18),
            ),
            10,
            mapOf(),
            Instant.now()
        )

        val currencies = props.getAllCurrencies()
        val dai = currencies.first { it.currencyId == "dai" }
        assertThat(dai.decimals).isEqualTo(10)

        val base = currencies.first { it.currencyId == "degen-base" }
        assertThat(base.decimals).isEqualTo(18)
    }

    companion object {
        val DAI = "0x6b175474e89094c44da98b954eedeac495271d0f"
    }

}
