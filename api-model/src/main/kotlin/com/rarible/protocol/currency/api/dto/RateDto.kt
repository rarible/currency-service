package com.rarible.protocol.currency.api.dto

import java.math.BigDecimal
import java.util.*

data class RateDto(
    val fromCurrencyId: String,
    val toCurrencyId: String,
    val rate: BigDecimal,
    val date: Date
)