package com.rarible.protocol.currency.api.configuration

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.format.annotation.DateTimeFormat
import scalether.domain.Address
import java.util.*

internal const val PREFIX = "common"

@ConstructorBinding
@ConfigurationProperties(PREFIX)
data class CurrencyApiProperties(
    val apiUrl: String,
    val coins: Map<String, Map<String, String>>,
    @DateTimeFormat(pattern = "yyyy-MM-dd") val historySince: Date
) {
    fun byAddress(platform: Platform?, address: Address): String? {
        return if(platform == null) {
            null
        } else {
            this.coins.entries.firstOrNull { (_, addresses) ->
                addresses[platform.name]
                    ?.let { hex -> Address.apply(hex) } == address
            }?.key
        }
    }
}


enum class Platform {
    ethereum;

    companion object {
        fun of(name: String): Platform? =  if(name == ethereum.name) {
            ethereum
        } else {
            null
        }
    }
}