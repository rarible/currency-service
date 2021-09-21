package com.rarible.protocol.currency.core.configuration

import com.rarible.protocol.currency.core.model.Blockchain
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import java.time.Instant

internal const val PREFIX = "common"

@ConstructorBinding
@ConfigurationProperties(PREFIX)
data class CurrencyApiProperties(
    val apiUrl: String,
    val coins: Map<String, Map<String, String>>,
    val historySince: Instant
) {
    fun byAddress(blockchain: Blockchain, address: String): String? {
        return this.coins.entries.firstOrNull { (_, addresses) ->
            addresses[blockchain.name] == address
        }?.key
    }
}
