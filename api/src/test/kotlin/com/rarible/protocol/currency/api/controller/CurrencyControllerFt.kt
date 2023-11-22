package com.rarible.protocol.currency.api.controller

import com.rarible.core.common.nowMillis
import com.rarible.core.test.data.randomBigDecimal
import com.rarible.core.test.ext.MongoTest
import com.rarible.protocol.currency.api.client.CurrencyApiClientFactory
import com.rarible.protocol.currency.api.client.CurrencyControllerApi
import com.rarible.protocol.currency.api.client.FixedCurrencyApiServiceUriProvider
import com.rarible.protocol.currency.api.client.NoopWebClientCustomizer
import com.rarible.protocol.currency.core.model.Rate
import com.rarible.protocol.currency.core.repository.RateRepository
import com.rarible.protocol.currency.dto.CurrencyRateDto
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.test.context.ActiveProfiles
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
@ActiveProfiles("test")
internal class CurrencyControllerFt {

    @LocalServerPort
    private var port: Int = 0

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
    fun `get actual`() = runBlocking<Unit> {

        val date = Instant.now().minusSeconds(60)
        val rateValue = BigDecimal("123.54")
        val rate = Rate.of("ethereum", date, rateValue)
        rateRepository.save(rate)

        val currencyRate = client.getCurrencyRate(
            "ETHEREUM",
            zeroAddress,
            date.minusSeconds(1).toEpochMilli()
        )?.awaitFirst()!!

        assertThat(currencyRate.rate).isEqualTo(rateValue)
        assertThat(currencyRate.abbreviation).isEqualTo("eth")
    }

    @Test
    fun `get actual polygon`() = runBlocking<Unit> {

        val date = Instant.now().minusSeconds(60)
        val rateValue = BigDecimal("123.54")
        val rate = Rate.of("matic-network", date, rateValue)
        rateRepository.save(rate)

        val currencyRate = client.getCurrencyRate(
            "POLYGON",
            zeroAddress,
            date.minusSeconds(1).toEpochMilli()
        )?.awaitFirst()!!

        assertThat(currencyRate.rate).isEqualTo(rateValue)
        assertThat(currencyRate.abbreviation).isEqualTo("matic")
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
            "POLYGON",
            zeroAddress,
            date.toEpochMilli()
        )?.awaitFirst()
    }

    private suspend fun saveRate(rateValue : String, at : Instant): BigDecimal {
        val rate = Rate.of("matic-network", at, BigDecimal(rateValue))
        rateRepository.save(rate)
        return rate.rate
    }

    @Test
    fun `get actual with other address type`() = runBlocking {

        val date = Instant.now().minusSeconds(60)
        val rateValue = BigDecimal("123.54")
        val rate = Rate.of("ethereum", date, rateValue)
        rateRepository.save(rate)

        val currencyRate = client.getCurrencyRate(
            "ETHEREUM",
            "0000000000000000000000000000000000000000",
            date.minusSeconds(1).toEpochMilli()
        )?.awaitFirst()

        assertEquals(currencyRate?.rate, rateValue)
    }

    @Test
    fun `get aliased currency rate`() = runBlocking<Unit> {

        val date = nowMillis().minusSeconds(60)
        val rateValue = BigDecimal("133")
        val rate = Rate.of("tezos", date, rateValue)
        rateRepository.save(rate)

        val currencyRate = client.getCurrencyRate(
            "TEZOS",
            "KT1EJkjatSNWD2NiPx8hivKnawxuyaVTwP6n",
            date.minusSeconds(1).toEpochMilli()
        )?.awaitFirst()!!

        assertThat(currencyRate.rate).isEqualTo(rateValue)
        assertThat(currencyRate.fromCurrencyId).isEqualTo("wtez")
        assertThat(currencyRate.abbreviation).isEqualTo("wxtz")
    }

    @Test
    fun `get usd wrapped currency rate`() = runBlocking<Unit> {
        val currencyRate = client.getCurrencyRate(
            "TEZOS",
            "KT18fp5rcTW7mbWDmzFwjLDUhs5MeJmagDSZ:17",
            nowMillis().toEpochMilli()
        )?.awaitFirst()!!

        assertThat(currencyRate.rate).isEqualTo(BigDecimal.ONE)
        assertThat(currencyRate.fromCurrencyId).isEqualTo("wrapped-usdc")
        assertThat(currencyRate.abbreviation).isEqualTo("xusd")
    }

    @Test
    fun `get last`() = runBlocking {
        val date = Instant.now().minusSeconds(60)
        val rateValue = BigDecimal("123.54")
        val rate = Rate.of("ethereum", date, rateValue)
        rateRepository.save(rate)

        val currencyRate = client.getCurrencyRate(
            "ETHEREUM",
            zeroAddress,
            date.plusSeconds(1).toEpochMilli()
        )?.block()

        assertEquals(currencyRate?.rate, rateValue)
    }

    @Test
    fun `get all currencies`() = runBlocking<Unit> {
        val date = Instant.now().minusSeconds(60)
        val rateValue = BigDecimal("123.54")
        val rate = Rate.of("weth", date, rateValue)
        rateRepository.save(rate)

        val currencies = client.allCurrencies?.awaitFirstOrNull()?.currencies!!

        val wethCurrencies = currencies.filter { it.currencyId == "weth" }
        assertThat(wethCurrencies).hasSize(6)

        val eth = wethCurrencies.find { it.blockchain == "ETHEREUM" }!!
        // Should be the same as for Eth despite absence in configuration
        val imx = wethCurrencies.find { it.blockchain == "IMMUTABLEX" }!!
        val poly = wethCurrencies.find { it.blockchain == "POLYGON" }!!
        val opt = wethCurrencies.find { it.blockchain == "OPTIMISM" }!!
        val mantle = wethCurrencies.find { it.blockchain == "MANTLE" }!!
        val arbitrum = wethCurrencies.find { it.blockchain == "ARBITRUM" }!!

        assertThat(eth.address).isEqualTo("0xc02aaa39b223fe8d0a0e5c4f27ead9083c756cc2")
        assertThat(eth.abbreviation).isEqualTo("weth")
        assertThat(eth.rate).isEqualTo(rateValue)

        assertThat(poly.address).isEqualTo("0x7ceb23fd6bc0add59e62ac25578270cff1b9f619")
        assertThat(poly.abbreviation).isEqualTo("weth")

        assertThat(imx.address).isEqualTo(eth.address)
        assertThat(imx.abbreviation).isEqualTo("weth")

        assertThat(opt.address).isEqualTo("0x4200000000000000000000000000000000000006")
        assertThat(opt.abbreviation).isEqualTo("weth")

        assertThat(mantle.address).isEqualTo("0xdeaddeaddeaddeaddeaddeaddeaddeaddead1111")
        assertThat(mantle.abbreviation).isEqualTo("weth")

        assertThat(arbitrum.address).isEqualTo("0x82af49447d8a07e3bd95bd0d56f35241523fbab1")
        assertThat(arbitrum.abbreviation).isEqualTo("weth")
    }

    @Test
    fun `get immutablex currency`() = runBlocking<Unit> {
        val date = Instant.now().minusSeconds(60)
        val rateValue = randomBigDecimal()
        val rate = Rate.of("ecomi", date, rateValue)
        rateRepository.save(rate)

        val currencyRate = client.getCurrencyRate(
            "IMMUTABLEX",
            "0xed35af169af46a02ee13b9d79eb57d6d68c1749e",
            date.plusSeconds(1).toEpochMilli()
        )?.block()

        assertEquals(currencyRate?.rate, rateValue)
    }

    @Test
    fun `get immutablex currency from ethereum`() = runBlocking<Unit> {
        val date = Instant.now().minusSeconds(60)
        val rateValue = randomBigDecimal()
        val rate = Rate.of("apecoin", date, rateValue)
        rateRepository.save(rate)

        val currencyRate = client.getCurrencyRate(
            "IMMUTABLEX", // not defined for IMX, but defined for ETHEREUM
            "0x4d224452801aced8b2f0aebe155379bb5d594381",
            date.plusSeconds(1).toEpochMilli()
        )?.block()

        assertEquals(currencyRate?.rate, rateValue)
    }

    @Test
    fun `should throw validate error`() {
        val badAddress = "${zeroAddress}1"
        val msg = "Unable to parse ETHEREUM or POLYGON address [$badAddress]"
        assertThrows<CurrencyControllerApi.ErrorGetCurrencyRate>(msg) {
            client.getCurrencyRate(
                "ETHEREUM",
                badAddress,
                Instant.now().toEpochMilli()
            ).block()
        }
    }
}
