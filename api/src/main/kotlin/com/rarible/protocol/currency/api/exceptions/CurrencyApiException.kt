package com.rarible.protocol.currency.api.exceptions

import com.rarible.protocol.currency.api.error.CurrencyApiErrorCodeDto
import org.springframework.http.HttpStatus

sealed class CurrencyApiException(
    message: String,
    val status: HttpStatus,
    val code: CurrencyApiErrorCodeDto
) : Exception(message)
