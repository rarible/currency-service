package com.rarible.protocol.currency.core.configuration

import com.rarible.protocol.currency.core.exceptions.CurrencyApiException
import com.rarible.protocol.currency.dto.CurrencyApiErrorDto
import com.rarible.protocol.currency.dto.CurrencyDto
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.http.HttpStatus
import scalether.domain.Address
import java.net.URI
import java.time.Duration
import java.time.Instant
import java.util.*

internal const val PREFIX = "common"

@ConstructorBinding
@ConfigurationProperties(PREFIX)
data class CurrencyApiProperties(
    val apiUrl: URI,
    val coins: Map<String, Map<String, String>>,
    val aliases: Map<String, String> = emptyMap(),
    val historySince: Instant,
    val request: RequestProperties = RequestProperties(),
    val proxyUrl: URI? = null,
    val clientType: ClientType = ClientType.FEIGN
) {

    fun byAddress(blockchain: String, address: String): String? {
        val extraCoins = extraCurrency[blockchain]

        return if (extraCoins?.containsKey(address.lowercase()) == true) {
            extraCoins[address.lowercase()]
        } else {
            this.coins.entries.firstOrNull { (_, addresses) ->
                val found = if (blockchain == "IMMUTABLEX") {
                    // If there is no ERC20 specific address for IMX, we can take it from ETHEREUM
                    // They use same addresses for IMX ERC20
                    addresses[blockchain] ?: addresses["ETHEREUM"]
                } else {
                    addresses[blockchain]
                }
                when (blockchain) {
                    "ETHEREUM",
                    "POLYGON",
                    "OPTIMISM",
                    "MANTLE",
                    "IMMUTABLEX" -> {
                        found?.let { Address.apply(it) } == parseAddress(address)
                    }
                    else -> {
                        found == address
                    }
                }
            }?.key
        }
    }

    private fun parseAddress(address: String): Address {
        try {
            return Address.apply(address)
        } catch (e: Throwable) {
            val msg = "Unable to parse ETHEREUM or POLYGON or IMMUTABLEX address [$address]"
            throw CurrencyApiException(
                message = msg,
                code = CurrencyApiErrorDto.Code.VALIDATION,
                status = HttpStatus.BAD_REQUEST
            )
        }
    }

    fun getRealCoin(alias: String): String {
        // if there is no alias it means we work with original coin
        return aliases[alias] ?: alias
    }

    fun getAllCurrencies(): List<CurrencyDto> {
        return coins.map { coin ->
            val coinId = coin.key
            val byBlockchain = coin.value.map {
                CurrencyDto(
                    currencyId = coinId,
                    alias = aliases[coinId],
                    blockchain = it.key,
                    address = it.value
                )
            }.associateByTo(TreeMap()) { it.blockchain }
            val eth = byBlockchain["ETHEREUM"]
            val imx = byBlockchain["IMMUTABLEX"]
            if (eth != null && imx == null) {
                // IMX has same currencies as Ethereum
                byBlockchain["IMMUTABLEX"] = eth.copy(blockchain = "IMMUTABLEX")
            }
            byBlockchain.values
        }.flatten()
    }

    private val extraCurrency = mapOf(
        "TEZOS" to mapOf("xtz" to "tezos")
    )
}

enum class ClientType {
    FEIGN,
    WEB
}

data class RequestProperties(
    val delay: Duration = Duration.ofSeconds(10),
    val errorDelay: Duration = Duration.ofSeconds(10),
    val attempts: Int = 3
)
