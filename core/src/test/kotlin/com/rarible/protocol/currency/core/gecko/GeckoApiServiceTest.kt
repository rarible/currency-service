package com.rarible.protocol.currency.core.gecko

import com.rarible.core.test.data.randomLong
import com.rarible.core.test.data.randomString
import com.rarible.protocol.currency.core.configuration.CurrencyApiProperties
import com.rarible.protocol.currency.core.configuration.RequestProperties
import feign.FeignException
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.http.HttpHeaders
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.core.publisher.Mono
import java.time.Duration

@ExtendWith(MockKExtension::class)
class GeckoApiServiceTest {
    @MockK
    private lateinit var geckoApi: GeckoApi

    private val requestProperties = mockk<RequestProperties>()

    private val properties = mockk<CurrencyApiProperties> {
        every { request } returns requestProperties
    }

    @InjectMockKs
    private lateinit var service: GeckoApiService

    @Test
    fun `retry - ok, 429`() = runBlocking<Unit> {
        val currencyId = randomString()
        val from = randomLong()
        val to = randomLong()

        every { requestProperties.attempts } returns 3
        every { requestProperties.errorDelay } returns Duration.ZERO

        every { geckoApi.history(currencyId, from, to) } returnsMany listOf(
            Mono.error(FeignException.TooManyRequests("error", mockk(), ByteArray(0), emptyMap())),
            Mono.error(WebClientResponseException(429, "error", HttpHeaders(), ByteArray(0), mockk())),
            Mono.just(HistoryResponse())
        )

        val result = service.history(currencyId, from, to)
        assertThat(result).isEqualTo(HistoryResponse())
    }

    @Test
    fun `retry - fail`() = runBlocking<Unit> {
        val currencyId = randomString()
        val from = randomLong()
        val to = randomLong()

        every { requestProperties.attempts } returns 3
        every { requestProperties.errorDelay } returns Duration.ZERO

        every { geckoApi.history(currencyId, from, to) } returnsMany listOf(
            Mono.error(FeignException.TooManyRequests("error", mockk(), ByteArray(0), emptyMap())),
            Mono.error(FeignException.TooManyRequests("error", mockk(), ByteArray(0), emptyMap())),
            Mono.error(WebClientResponseException(429, "error", HttpHeaders(), ByteArray(0), mockk())),
            Mono.error(WebClientResponseException(429, "error", HttpHeaders(), ByteArray(0), mockk())),
            Mono.just(HistoryResponse())
        )

        assertThrows<WebClientResponseException> {
            service.history(currencyId, from, to)
        }
    }
}