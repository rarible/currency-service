package com.rarible.protocol.currency.core.configuration

import com.rarible.core.model.type.Blockchain
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
    fun byAddress(platform: Blockchain, address: Address): String? {
        return this.coins.entries.firstOrNull { (_, addresses) ->
            addresses[platform.name]
                ?.let { hex -> Address.apply(hex) } == address
        }?.key
    }
}