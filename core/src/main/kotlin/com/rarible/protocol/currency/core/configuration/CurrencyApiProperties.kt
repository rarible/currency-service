package com.rarible.protocol.currency.core.configuration

import com.rarible.protocol.currency.core.exceptions.CurrencyApiException
import com.rarible.protocol.currency.core.model.Blockchain
import com.rarible.protocol.currency.dto.BlockchainDto
import com.rarible.protocol.currency.dto.CurrencyApiErrorDto
import com.rarible.protocol.currency.dto.CurrencyDto
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.http.HttpStatus
import scalether.domain.Address
import java.net.URI
import java.time.Duration
import java.time.Instant

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
    fun byAddress(blockchain: Blockchain, address: String): String? {
        val extraCoins = extraCurrency[blockchain]

        return if (extraCoins?.containsKey(address.toLowerCase()) == true) {
            extraCoins[address.toLowerCase()]
        } else {
            this.coins.entries.firstOrNull { (_, addresses) ->
                when (blockchain) {
                    Blockchain.ETHEREUM, Blockchain.POLYGON, Blockchain.IMMUTABLEX -> {
                        val currencyAddress = try {
                            Address.apply(address)
                        } catch (e: Throwable) {
                            val msg = "Unable to parse ETHEREUM or POLYGON or IMMUTABLEX address [$address]"
                            throw CurrencyApiException(
                                message = msg,
                                code = CurrencyApiErrorDto.Code.VALIDATION,
                                status = HttpStatus.BAD_REQUEST
                            )
                        }
                        addresses[blockchain.name]?.let { Address.apply(it) } == currencyAddress
                    }
                    Blockchain.FLOW, Blockchain.SOLANA, Blockchain.TEZOS, Blockchain.APTOS -> {
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

    fun getAllCurrencies(): List<CurrencyDto> {
        return coins.map { coin ->
            coin.value.map {
                CurrencyDto(
                    currencyId = coin.key,
                    alias = aliases[coin.key],
                    blockchain = BlockchainDto.valueOf(it.key),
                    address = it.value
                )
            }
        }.flatten()
    }

    private val extraCurrency = mapOf(
        Blockchain.TEZOS to mapOf("xtz" to "tezos")
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
