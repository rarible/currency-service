package com.rarible.protocol.currency.api.client

import com.rarible.protocol.currency.api.ApiClient
import org.springframework.boot.web.reactive.function.client.WebClientCustomizer

open class CurrencyApiClientFactory(
    private val uriProvider: CurrencyApiServiceUriProvider,
    private val webClientCustomizer: WebClientCustomizer
) {
    fun createCurrencyApiClient(): CurrencyControllerApi {
        return CurrencyControllerApi(createApiClient())
    }

    private fun createApiClient(): ApiClient {
        return ApiClient(webClientCustomizer)
            .setBasePath(uriProvider.getUri().toASCIIString())
    }
}

