package com.rarible.protocol.currency.core.repository

import com.rarible.core.test.containers.MongodbReactiveBaseTest
import com.rarible.protocol.currency.core.model.Rate
import com.rarible.protocol.currency.core.repository.RateRepository
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions
import org.assertj.core.api.InstanceOfAssertFactory
import org.bson.types.ObjectId
import org.junit.jupiter.api.Test
import org.testcontainers.junit.jupiter.Testcontainers
import java.math.BigDecimal
import java.util.*


@Testcontainers
class RateRepositoryTest : MongodbReactiveBaseTest() {
    private val repo = RateRepository(createReactiveMongoTemplate())

    @Test
    fun `should save and find last`() = runBlocking<Unit> {
        val rate1 = createRate("ethereum")
        val rate2 = createRate("dai")
        val rate3 = createRate("ethereum")
        repo.saveAll(listOf(rate1, rate2, rate3))

        var findLast = repo.findLast("ethereum")
        Assertions.assertThat(findLast).isNotNull
        Assertions.assertThat(findLast!!.rate).isEqualTo(rate3.rate)

        findLast = repo.findLast("dai")
        Assertions.assertThat(findLast).isNotNull
        Assertions.assertThat(findLast!!.rate).isEqualTo(rate2.rate)

        val rate4 = createRate("dai")
        val rate5 = createRate("ethereum")
        repo.saveAll(listOf(rate4, rate5))
        findLast = repo.findLast("ethereum")
        Assertions.assertThat(findLast).isNotNull
        Assertions.assertThat(findLast!!.rate).isEqualTo(rate5.rate)

        findLast = repo.findLast("dai")
        Assertions.assertThat(findLast).isNotNull
        Assertions.assertThat(findLast!!.rate).isEqualTo(rate4.rate)

    }


    fun createRate(currencyId: String) = Rate(
        ObjectId.get(), currencyId, Date(), BigDecimal.valueOf(Random(Date().time).nextDouble())
    )
}
