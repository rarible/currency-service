package com.rarible.protocol.currency.api

import com.rarible.protocol.currency.api.dto.RateDto
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import reactor.core.publisher.Mono
import scalether.domain.Address
import java.util.*


interface CurrencyApi {
    @GetMapping(value = ["/rate"])
    fun getRate(
        @RequestParam("blockchain")
        blockchain: String,

        @RequestParam("address")
        address: Address,

        @RequestParam("at")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        at: Date
    ): Mono<RateDto>
}