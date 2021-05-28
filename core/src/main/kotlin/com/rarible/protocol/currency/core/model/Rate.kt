package com.rarible.protocol.currency.core.model

import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Version
import org.springframework.data.mongodb.core.index.CompoundIndex
import org.springframework.data.mongodb.core.mapping.Document
import java.math.BigDecimal
import java.time.Instant

@Document
@CompoundIndex(
    def = "{'currencyId': 1, 'date': -1}"
)
data class Rate (
    @Id
    val id: ObjectId,

    val currencyId: String,

    val date: Instant,

    val rate: BigDecimal,

    @Version
    val version: Long? = null
) {
    companion object {
        fun of(currencyId: String, date: Instant, rate: BigDecimal) =
            Rate(ObjectId.get(), currencyId, date, rate)
    }
}
