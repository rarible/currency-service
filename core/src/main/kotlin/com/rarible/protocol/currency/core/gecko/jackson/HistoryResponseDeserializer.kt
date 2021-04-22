package com.rarible.protocol.currency.core.gecko.jackson

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import com.rarible.protocol.currency.core.gecko.HistoryResponse
import com.rarible.protocol.currency.core.gecko.Price
import java.util.*


class HistoryResponseDeserializer: JsonDeserializer<HistoryResponse>() {
    override fun deserialize(parser: JsonParser, ctx: DeserializationContext): HistoryResponse {
        val node: JsonNode = parser.getCodec().readTree(parser)
        val pricesJson = node["prices"] as ArrayNode

        val prices: List<Price> = pricesJson.map {
            val price = it as ArrayNode
            val date = Date(price[0].asLong())
            val rate = price[1].asDouble().toBigDecimal()
            Price(date, rate)
        }.toList()
        return HistoryResponse(prices)
    }
}