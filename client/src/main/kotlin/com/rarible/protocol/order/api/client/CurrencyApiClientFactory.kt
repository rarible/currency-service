package com.rarible.protocol.order.api.client

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.rarible.protocol.currency.api.CurrencyApi
import ru.roborox.reactive.feign.FeignHelper

class CurrencyApiClientFactory(
    private val serviceUriProvider: CurrencyApiServiceUriProvider
) {
    private val mapper = ObjectMapper().registerKotlinModule()

    fun createCurrencyApiClient(): CurrencyApi {
        val host = serviceUriProvider.provideUri()
        return FeignHelper.createClient(mapper, host.resolve("/v0.1/currency").toASCIIString())
    }

}

