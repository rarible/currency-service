package com.rarible.protocol.currency.core.gecko

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import io.netty.channel.ChannelOption
import io.netty.channel.epoll.EpollChannelOption
import io.netty.handler.timeout.ReadTimeoutHandler
import io.netty.handler.timeout.WriteTimeoutHandler
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.client.reactive.ClientHttpConnector
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.util.unit.DataSize
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.ExchangeStrategies
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientException
import org.springframework.web.reactive.function.client.bodyToMono
import org.springframework.web.util.DefaultUriBuilderFactory
import reactor.core.publisher.Mono
import reactor.netty.http.client.HttpClient
import reactor.netty.resources.ConnectionProvider
import reactor.netty.transport.ProxyProvider
import java.net.URI
import java.time.Duration
import java.util.concurrent.TimeUnit

class GeckoApiImpl(
    baseUrl: URI,
    proxyUrl: URI? = null
) : GeckoApi {

    private val mapper = ObjectMapper()
        .registerModule(KotlinModule())
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .setSerializationInclusion(JsonInclude.Include.NON_NULL)

    private val uriBuilderFactory = DefaultUriBuilderFactory(baseUrl.toASCIIString()).apply {
        encodingMode = DefaultUriBuilderFactory.EncodingMode.NONE
    }
    private val transport = initTransport(baseUrl, proxyUrl)

    override fun currentPrice(coinId: String, vsCurrency: String): Mono<Map<String, Map<String, Double>>> {
        val uri = uriBuilderFactory.builder().run {
            path(CURRENT_PRICE_PATH)
            queryParam(ID_PARAM, coinId)
            queryParam(VS_CURRENCIES_PARAM, vsCurrency)
            build()
        }
        return get(uri)
    }

    override fun coinsList(): Mono<List<Coin>> {
        val uri = uriBuilderFactory.builder().run {
            path(COINS_LIST_PATH)
            build()
        }
        return get(uri)
    }

    override fun history(currencyId: String, from: Long, to: Long, vsCurrency: String): Mono<HistoryResponse> {
        val uri = uriBuilderFactory.builder().run {
            path(HISTORY_PATH.replace("{$ID_PARAM}", currencyId))
            queryParam(FROM_PARAM, from)
            queryParam(TO_PARAM, to)
            queryParam(VS_CURRENCY_PARAM, vsCurrency)
            build()
        }
        return get(uri)
    }

    private inline fun <reified T>  get(uri: URI): Mono<T> {
        return transport
            .get()
            .uri(uri)
            .exchangeToMono { getResult(it, uri) }
    }

    private inline fun <reified T> getResult(response: ClientResponse, uri: URI): Mono<T> {
        val body = response.bodyToMono<ByteArray>()
        return when (response.statusCode()) {
            HttpStatus.OK -> {
                body.map {
                    mapper.readValue(it, T::class.java)
                }
            }
            else -> {
                val httpCode = response.statusCode().value()
                Mono.error(GeckoApiException("[$httpCode] during [GET] to [${uri.toASCIIString()}]"))
            }
        }
    }

    private fun initTransport(endpoint: URI, proxy: URI?): WebClient {
        return WebClient.builder().run {
            clientConnector(clientConnector(proxy))
            exchangeStrategies(
                ExchangeStrategies.builder()
                    .codecs { it.defaultCodecs().maxInMemorySize(DEFAULT_MAX_BODY_SIZE) }
                    .build()
            )
            baseUrl(endpoint.toASCIIString())
            build()
        }
    }

    private fun clientConnector(proxy: URI?): ClientHttpConnector {
        val provider = ConnectionProvider.builder("open-sea-connection-provider")
            .maxConnections(50)
            .pendingAcquireMaxCount(-1)
            .maxIdleTime(DEFAULT_TIMEOUT)
            .maxLifeTime(DEFAULT_TIMEOUT)
            .lifo()
            .build()

        val client = HttpClient
            .create(provider)
            .option(ChannelOption.SO_KEEPALIVE, true)
            .option(EpollChannelOption.TCP_KEEPIDLE, 300)
            .option(EpollChannelOption.TCP_KEEPINTVL, 60)
            .option(EpollChannelOption.TCP_KEEPCNT, 8)
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, DEFAULT_TIMEOUT_MILLIS.toInt())
            .doOnConnected { connection ->
                connection.addHandlerLast(ReadTimeoutHandler(DEFAULT_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS))
                connection.addHandlerLast(WriteTimeoutHandler(DEFAULT_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS))
            }
            .responseTimeout(DEFAULT_TIMEOUT)

        val finalClient = if (proxy != null) {
            logger.info("Proxy was configured for GeckoApi Web client")
            client
                .proxy { option ->
                    val userInfo = proxy.userInfo.split(":")
                    option.type(ProxyProvider.Proxy.HTTP).host(proxy.host).username(userInfo[0]).password { userInfo[1] }.port(proxy.port)
                }
        } else {
            client
        }
        return ReactorClientHttpConnector(finalClient)
    }

    class GeckoApiException(message: String) : WebClientException(message)

    private companion object {
        val logger: Logger = LoggerFactory.getLogger(GeckoApiImpl::class.java)
        val DEFAULT_MAX_BODY_SIZE = DataSize.ofMegabytes(10).toBytes().toInt()
        val DEFAULT_TIMEOUT: Duration = Duration.ofSeconds(60)
        val DEFAULT_TIMEOUT_MILLIS: Long = DEFAULT_TIMEOUT.toMillis()
    }
}