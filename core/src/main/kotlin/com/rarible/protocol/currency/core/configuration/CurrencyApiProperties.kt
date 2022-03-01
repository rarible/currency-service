package com.rarible.protocol.currency.core.configuration

import com.rarible.protocol.currency.core.model.Blockchain
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
    val aliases: Map<String, String> = emptyMap(),
    val historySince: Instant
) {
    fun byAddress(blockchain: Blockchain, address: String): String? {
        val extraCoins = extraCurrency[blockchain]

        return if (extraCoins?.containsKey(address.toLowerCase()) == true) {
            extraCoins[address.toLowerCase()]
        } else {
            this.coins.entries.firstOrNull { (_, addresses) ->
                when (blockchain) {
                    Blockchain.ETHEREUM, Blockchain.POLYGON -> {
                        addresses[blockchain.name]?.let { Address.apply(it) } == Address.apply(address)
                    }
                    Blockchain.FLOW, Blockchain.SOLANA, Blockchain.TEZOS -> {
                        addresses[blockchain.name] == address
                    }
                }
            }?.key
        }
    }

    fun getRealCoin(alias: String): String {
        // if there is no alias it means we work with original coin
        return aliases[alias] ?: alias
    }

    private val extraCurrency = mapOf(
        Blockchain.TEZOS to mapOf("xtz" to "tezos")
    )
}
