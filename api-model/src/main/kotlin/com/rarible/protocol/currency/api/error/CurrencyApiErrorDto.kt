package com.rarible.protocol.currency.api.error

import io.swagger.v3.oas.annotations.media.Schema

@Schema(name = "CurrencyApiError")
class CurrencyApiErrorDto(
    val status: Int,
    val code: CurrencyApiErrorCodeDto,
    val message: String
)