package com.rarible.protocol.currency.core.configuration

import com.rarible.ethereum.domain.Blockchain
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import scalether.domain.Address
import java.time.Instant

internal const val PREFIX = "common"

@ConstructorBinding
@ConfigurationProperties(PREFIX)
data class CurrencyApiProperties(
    val apiUrl: String,
    val coins: Map<String, Map<String, String>>,
    val historySince: Instant
) {
    fun byAddress(platform: Blockchain, address: Address): String? {
        return this.coins.entries.firstOrNull { (_, addresses) ->
            addresses[platform.name]
                ?.let { hex -> Address.apply(hex) } == address
        }?.key
    }
}
