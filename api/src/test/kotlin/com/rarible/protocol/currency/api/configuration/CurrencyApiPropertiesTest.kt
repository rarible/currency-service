package com.rarible.protocol.currency.api.configuration

import com.rarible.protocol.currency.core.configuration.CurrencyApiProperties
import com.rarible.protocol.currency.core.model.Blockchain
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import scalether.domain.Address
import java.time.Instant

class CurrencyApiPropertiesTest {

    @Test
    fun `should get correct coin id by address`() {
        val props = CurrencyApiProperties(
            "",
            mapOf(
                "ethereum" to mapOf("ETHEREUM" to "0x0000000000000000000000000000000000000000"),
                "dai" to mapOf("ETHEREUM" to DAI)
            ),
            Instant.now()
        )

        Assertions.assertEquals("ethereum", props.byAddress(Blockchain.ETHEREUM, Address.ZERO().prefixed()))
        Assertions.assertEquals("dai", props.byAddress(Blockchain.ETHEREUM, Address.apply(DAI).prefixed()))
    }

    companion object {
        val DAI = "0x6b175474e89094c44da98b954eedeac495271d0f"
    }

}
