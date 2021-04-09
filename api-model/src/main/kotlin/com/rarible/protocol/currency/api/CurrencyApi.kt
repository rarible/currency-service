package com.rarible.protocol.currency.api

import com.rarible.protocol.currency.api.dto.RateDto
import org.springframework.web.bind.annotation.GetMapping
import reactor.core.publisher.Mono
import scalether.domain.Address
import java.util.*


interface CurrencyApi {
    @GetMapping(value = ["/rate"])
    fun getRate(platform: String, address: Address, date: Date): Mono<RateDto>
}