package com.rarible.protocol.currency.api.controller

import com.rarible.core.common.nowMillis
import com.rarible.core.test.ext.MongoTest
import com.rarible.protocol.currency.api.client.CurrencyApiClientFactory
import com.rarible.protocol.currency.api.client.CurrencyControllerApi
import com.rarible.protocol.currency.api.client.FixedCurrencyApiServiceUriProvider
import com.rarible.protocol.currency.api.client.NoopWebClientCustomizer
import com.rarible.protocol.currency.core.model.Rate
import com.rarible.protocol.currency.core.repository.RateRepository
import com.rarible.protocol.currency.dto.BlockchainDto
import com.rarible.protocol.currency.dto.CurrencyRateDto
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import java.math.BigDecimal
import java.net.URI
import java.time.Instant
import org.junit.jupiter.api.assertThrows

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
internal class CurrencyControllerFt(
    @LocalServerPort
    val port: Int
) {

    val zeroAddress = "0x0000000000000000000000000000000000000000"

    @Autowired
    private lateinit var rateRepository: RateRepository

    lateinit var client: CurrencyControllerApi

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
        )?.awaitFirst()

        assertEquals(currencyRate?.rate, rateValue)
    }

    @Test
    fun `get actual polygon`() = runBlocking {

        val date = Instant.now().minusSeconds(60)
        val rateValue = BigDecimal("123.54")
        val rate = Rate.of("matic-network", date, rateValue)
        rateRepository.save(rate)

        val currencyRate = client.getCurrencyRate(
            BlockchainDto.POLYGON,
            zeroAddress,
            date.minusSeconds(1).toEpochMilli()
        )?.awaitFirst()

        assertEquals(currencyRate?.rate, rateValue)
    }

    @Test
    fun `get actual rate with date filter`() = runBlocking<Unit> {

        val dateBefore1 = nowMillis().minusSeconds(120)
        val dateBefore2 = nowMillis().minusSeconds(60)
        val dateAfter = nowMillis().plusSeconds(900)

        saveRate("111.11", dateBefore1)
        val rateValue = saveRate("222.22", dateBefore2)
        val rateValueLatest = saveRate("333.33", dateAfter)

        val currencyRateBeforeDate = getRateForDate(dateBefore2.plusSeconds(1))
        assertThat(currencyRateBeforeDate?.rate).isEqualTo(rateValue)

        val currencyRateLatest = getRateForDate(dateAfter.plusSeconds(1))
        assertThat(currencyRateLatest?.rate).isEqualTo(rateValueLatest)

        val currencyRateLatestBeforeDate = getRateForDate(dateAfter.minusSeconds(1))
        assertThat(currencyRateLatestBeforeDate?.rate).isEqualTo(rateValue)
    }
;
    private suspend fun getRateForDate(date: Instant): CurrencyRateDto? {
        return client.getCurrencyRate(
            BlockchainDto.POLYGON,
            zeroAddress,
            date.toEpochMilli()
        )?.awaitFirst()
    }

    private suspend fun saveRate(rateValue : String, at : Instant): BigDecimal {
        val rateValue = BigDecimal(rateValue)
        val rate = Rate.of("matic-network", at, rateValue)
        rateRepository.save(rate)
        return rateValue
    }

    @Test
    fun `get actual with other address type`() = runBlocking {

        val date = Instant.now().minusSeconds(60)
        val rateValue = BigDecimal("123.54")
        val rate = Rate.of("ethereum", date, rateValue)
        rateRepository.save(rate)

        val currencyRate = client.getCurrencyRate(
            BlockchainDto.ETHEREUM,
            "0000000000000000000000000000000000000000",
            date.minusSeconds(1).toEpochMilli()
        )?.awaitFirst()

        assertEquals(currencyRate?.rate, rateValue)
    }

    @Test
    fun `get aliased currency rate`() = runBlocking {

        val date = nowMillis().minusSeconds(60)
        val rateValue = BigDecimal("133")
        val rate = Rate.of("tezos", date, rateValue)
        rateRepository.save(rate)

        val currencyRate = client.getCurrencyRate(
            BlockchainDto.TEZOS,
            "KT1EJkjatSNWD2NiPx8hivKnawxuyaVTwP6n",
            date.minusSeconds(1).toEpochMilli()
        )?.awaitFirst()

        assertEquals(currencyRate?.rate, rateValue)
        assertEquals(currencyRate?.fromCurrencyId, "wtez")
    }

    @Test
    fun `get usd wrapped currency rate`() = runBlocking {
        val currencyRate = client.getCurrencyRate(
            BlockchainDto.TEZOS,
            "KT18fp5rcTW7mbWDmzFwjLDUhs5MeJmagDSZ:17",
            nowMillis().toEpochMilli()
        )?.awaitFirst()

        assertEquals(currencyRate?.rate, BigDecimal.ONE)
        assertEquals(currencyRate?.fromCurrencyId, "wrapped-usdc")
    }

    @Test
    fun `get last`() = runBlocking {
        val date = Instant.now().minusSeconds(60)
        val rateValue = BigDecimal("123.54")
        val rate = Rate.of("ethereum", date, rateValue)
        rateRepository.save(rate)

        val currencyRate = client.getCurrencyRate(
            BlockchainDto.ETHEREUM,
            zeroAddress,
            date.plusSeconds(1).toEpochMilli()
        )?.block()

        assertEquals(currencyRate?.rate, rateValue)
    }

    @Test
    fun `get all currencies`() = runBlocking<Unit> {
        val currencies = client.allCurrencies?.awaitFirstOrNull()?.currencies!!

        val wethCurrencies = currencies.filter { it.currencyId == "weth" }
        assertThat(wethCurrencies).hasSize(2)

        val eth = wethCurrencies.find { it.blockchain == BlockchainDto.ETHEREUM }!!
        val poly = wethCurrencies.find { it.blockchain == BlockchainDto.POLYGON }!!

        assertThat(eth.address).isEqualTo("0xc02aaa39b223fe8d0a0e5c4f27ead9083c756cc2")
        assertThat(poly.address).isEqualTo("0x7ceb23fd6bc0add59e62ac25578270cff1b9f619")
    }

    @Test
    fun `should throw validate error`() {
        val badAddress = "${zeroAddress}1"
        val msg = "Unable to parse ETHEREUM or POLYGON address [$badAddress]"
        assertThrows<CurrencyControllerApi.ErrorGetCurrencyRate>(msg) {
            client.getCurrencyRate(
                BlockchainDto.ETHEREUM,
                badAddress,
                Instant.now().toEpochMilli()
            ).block()
        }
    }
}
