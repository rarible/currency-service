package com.rarible.protocol.currency.core.repository

import com.rarible.protocol.currency.core.model.Rate
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrDefault
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.findById
import org.springframework.data.mongodb.core.findOne
import org.springframework.data.mongodb.core.index.Index
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.gt
import org.springframework.data.mongodb.core.query.isEqualTo
import java.util.*

class RateRepository(
    private val template: ReactiveMongoTemplate
) {
    suspend fun createIndexes() {
        template.indexOps(Rate::class.java).ensureIndex(
            Index()
                .on("${Rate::currencyId}", Sort.Direction.ASC)
                .on("${Rate::date}", Sort.Direction.ASC)
        ).awaitFirst()
    }

    suspend fun save(storageModel: Rate): Rate {
        return template.save(storageModel).awaitFirst()
    }

    suspend fun saveAll(rates: List<Rate>): List<Rate> =
        template.insertAll(rates).collectList().awaitFirstOrDefault(emptyList())

    suspend fun get(id: String): Rate? {
        return template.findById<Rate>(id).awaitFirstOrNull()
    }

    suspend fun getRate(currencyId: String, date: Date): Rate? {
        return template.findOne<Rate>(
            Query(
                Criteria().andOperator(
                    Rate::currencyId isEqualTo currencyId,
                    Rate::date gt date
                )
            ).with(
                Sort.by(Sort.Direction.DESC, Rate::date.name)
            )
        ).awaitFirstOrNull()
    }

    suspend fun findLast(currencyId: String): Rate? {
        return template.findOne<Rate>(
            Query(
                Criteria().andOperator(
                    Rate::currencyId isEqualTo currencyId
                )
            ).with(
                Sort.by(Sort.Direction.DESC, Rate::date.name)
            )
        ).awaitFirstOrNull()
    }


}