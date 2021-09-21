package com.rarible.protocol.currency.api.client

import java.net.URI

interface CurrencyApiServiceUriProvider {
    fun getUri(): URI
}
