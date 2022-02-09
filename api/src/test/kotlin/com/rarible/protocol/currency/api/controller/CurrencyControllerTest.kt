package com.rarible.protocol.currency.api.controller

import com.rarible.core.test.ext.MongoTest
import com.rarible.protocol.currency.api.client.CurrencyApiClientFactory
import com.rarible.protocol.currency.api.client.CurrencyControllerApi
import com.rarible.protocol.currency.api.client.FixedCurrencyApiServiceUriProvider
import com.rarible.protocol.currency.api.client.NoopWebClientCustomizer
import com.rarible.protocol.currency.core.model.Rate
import com.rarible.protocol.currency.core.repository.RateRepository
import com.rarible.protocol.currency.dto.BlockchainDto
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import java.math.BigDecimal
import java.net.URI
import java.time.Instant

@MongoTest
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = [
        "application.environment = e2e",
        "spring.cloud.service-registry.auto-registration.enabled = false",
        "spring.cloud.discovery.enabled = false",
        "spring.cloud.consul.config.enabled = false",
        "logging.logstash.tcp-socket.enabled = false"
    ]
)
internal class CurrencyControllerTest(
    @LocalServerPort
    val port: Int
) {
    val zeroAddress = "0x0000000000000000000000000000000000000000"

    @Autowired
    private lateinit var rateRepository: RateRepository

    private var client: CurrencyControllerApi? = null

    @BeforeEach
    fun beforeEach() = runBlocking<Unit> {
        val uri = URI.create("http://localhost:${port}")
        val clientFactory = CurrencyApiClientFactory(FixedCurrencyApiServiceUriProvider(uri), NoopWebClientCustomizer())
        client = clientFactory.createCurrencyApiClient()
        rateRepository.dropCollection()
    }

    @Test
    fun `get actual`() = runBlocking {

        val date = Instant.now().minusSeconds(60)
        val rateValue = BigDecimal("123.54")
        val rate = Rate.of("ethereum", date, rateValue)
        rateRepository.save(rate)

        val currencyRate = client?.getCurrencyRate(
            BlockchainDto.ETHEREUM,
            zeroAddress,
            date.minusSeconds(1).toEpochMilli()
        )?.block()

        assertEquals(currencyRate?.rate, rateValue)
    }

    @Test
    fun `get actual polygon`() = runBlocking {

        val date = Instant.now().minusSeconds(60)
        val rateValue = BigDecimal("123.54")
        val rate = Rate.of("matic-network", date, rateValue)
        rateRepository.save(rate)

        val currencyRate = client?.getCurrencyRate(
            BlockchainDto.POLYGON,
            zeroAddress,
            date.minusSeconds(1).toEpochMilli()
        )?.block()

        assertEquals(currencyRate?.rate, rateValue)
    }

    @Test
    fun `get actual with other address type`() = runBlocking {

        val date = Instant.now().minusSeconds(60)
        val rateValue = BigDecimal("123.54")
        val rate = Rate.of("ethereum", date, rateValue)
        rateRepository.save(rate)

        val currencyRate = client?.getCurrencyRate(
            BlockchainDto.ETHEREUM,
            "0000000000000000000000000000000000000000",
            date.minusSeconds(1).toEpochMilli()
        )?.block()

        assertEquals(currencyRate?.rate, rateValue)
    }

    @Test
    fun `get last`() = runBlocking {
        val date = Instant.now().minusSeconds(60)
        val rateValue = BigDecimal("123.54")
        val rate = Rate.of("ethereum", date, rateValue)
        rateRepository.save(rate)

        val currencyRate = client?.getCurrencyRate(
            BlockchainDto.ETHEREUM,
            zeroAddress,
            date.plusSeconds(1).toEpochMilli()
        )?.block()

        assertEquals(currencyRate?.rate, rateValue)
    }
}
