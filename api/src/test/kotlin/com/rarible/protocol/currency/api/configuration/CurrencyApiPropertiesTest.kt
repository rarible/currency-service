package com.rarible.protocol.currency.api.configuration

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import scalether.domain.Address
import java.util.*


class CurrencyApiPropertiesTest {

    @Test
    fun `should get correct coin id by address`() {
        val props = CurrencyApiProperties(
            "",
            mapOf(
                "ethereum" to mapOf("ETHEREUM" to "0x0000000000000000000000000000000000000000"),
                "dai" to mapOf("ETHEREUM" to DAI)
            ),
            Date()
        )

        Assertions.assertEquals("ethereum", props.byAddress(Platform.ETHEREUM, Address.ZERO()))
        Assertions.assertEquals("dai", props.byAddress(Platform.ETHEREUM, Address.apply(DAI)))
        Assertions.assertNull(props.byAddress(Platform.of("binance"), Address.ZERO()))
    }

    companion object {
        val DAI = "0x6b175474e89094c44da98b954eedeac495271d0f"
    }

}