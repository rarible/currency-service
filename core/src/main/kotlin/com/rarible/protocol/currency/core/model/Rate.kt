package com.rarible.protocol.currency.core.model

import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Version
import org.springframework.data.mongodb.core.index.CompoundIndex
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDateTime
import java.util.*

@Document
@CompoundIndex(
    def = "{'currencyId': 1, 'date': -1}"
)
data class Rate (
    @Id
    val id: ObjectId,

    val currencyId: String,

    val date: Date,

    val rate: BigDecimal,

    @Version
    val version: Long? = null
) {
    companion object {
        fun of(currencyId: String, date: Date, rate: BigDecimal) =
            Rate(ObjectId.get(), currencyId, date, rate)
    }
}