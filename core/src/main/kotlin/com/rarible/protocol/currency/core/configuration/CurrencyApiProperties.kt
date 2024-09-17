package com.rarible.protocol.currency.core.configuration

import com.rarible.protocol.currency.core.exceptions.CurrencyApiException
import com.rarible.protocol.currency.dto.CurrencyApiErrorDto
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
    val coins: Map<String, Map<String, List<String>>>,
    val decimals: Map<String, Map<String, Int>>,
    val defaultCurrencyDecimals: Int,
    val aliases: Map<String, String> = emptyMap(),
    val historySince: Instant,
    val request: RequestProperties = RequestProperties(),
    val proxyUrl: URI? = null,
    val clientType: ClientType = ClientType.FEIGN,
    val abbreviations: Map<String, String> = emptyMap()
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
                    "CHILIZ",
                    "ARBITRUM",
                    "ZKSYNC",
                    "LIGHTLINK",
                    "IMMUTABLEX" -> {
                        found?.any { Address.apply(it) == parseAddress(address) } == true
                    }
                    else -> {
                        found?.any { it == address } == true
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

    fun getAbbreviation(alias: String): String? {
        return abbreviations[alias]
    }

    fun getAllCurrencies(): List<Currency> {
        return coins.map { coin ->
            val coinId = coin.key
            val byBlockchain = coin.value.map { value ->
                val currencies = value.value.map {
                    Currency(
                        currencyId = coinId,
                        alias = aliases[coinId],
                        blockchain = value.key,
                        decimals = decimals[coinId]?.get(value.key) ?: defaultCurrencyDecimals,
                        address = it,
                        abbreviation = getAbbreviation(coinId)
                    )
                }
                value.key to currencies
            }.toMap().toMutableMap()

            val eth = byBlockchain["ETHEREUM"]
            val imx = byBlockchain["IMMUTABLEX"]
            if (eth != null && imx == null) {
                // IMX has same currencies as Ethereum
                byBlockchain["IMMUTABLEX"] = eth.map { it.copy(blockchain = "IMMUTABLEX") }
            }
            byBlockchain.values
        }.flatten().flatten()
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

data class Currency(
    val currencyId: String,
    val address: String,
    val blockchain: String,
    val decimals: Int,
    val alias: String? = null,
    val abbreviation: String? = null,
)
