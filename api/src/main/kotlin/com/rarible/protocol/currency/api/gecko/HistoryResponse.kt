package com.rarible.protocol.currency.api.gecko

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.rarible.protocol.currency.api.gecko.jackson.HistoryResponseDeserializer
import java.math.BigDecimal
import java.util.*

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonDeserialize(using = HistoryResponseDeserializer::class)
data class HistoryResponse(
    val prices: List<Pair<Date, BigDecimal>> = emptyList()
)