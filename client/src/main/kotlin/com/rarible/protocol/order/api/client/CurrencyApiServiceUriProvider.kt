package com.rarible.protocol.order.api.client

import java.net.URI

class CurrencyApiServiceUriProvider(
    private val environment: String
) {
    fun provideUri(): URI {
        return URI.create(String.format("http://%s-protocol-currency-api:8080", environment))
    }
}