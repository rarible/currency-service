package com.rarible.protocol.currency.core.exceptions

import com.rarible.protocol.currency.dto.CurrencyApiErrorDto
import org.springframework.http.HttpStatus

data class CurrencyApiException(
    override val message: String,
    val status: HttpStatus,
    val code: CurrencyApiErrorDto.Code
) : Exception(message)
