package com.rarible.protocol.currency.api.error

import io.swagger.v3.oas.annotations.media.Schema

@Schema(name = "CurrencyApiErrorCode")
enum class CurrencyApiErrorCodeDto {
    UNKNOWN,
    VALIDATION,
    FIRST_TEMPLATE_OBJECT_NOT_FOUND,
    SECOND_TEMPLATE_OBJECT_NOT_FOUND,
}