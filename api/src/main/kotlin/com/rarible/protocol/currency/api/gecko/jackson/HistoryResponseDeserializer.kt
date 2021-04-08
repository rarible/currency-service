package com.rarible.protocol.currency.api.gecko.jackson

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import com.rarible.protocol.currency.api.gecko.HistoryResponse
import java.util.*


class HistoryResponseDeserializer: JsonDeserializer<HistoryResponse>() {
    override fun deserialize(parser: JsonParser, ctx: DeserializationContext): HistoryResponse {
        val node: JsonNode = parser.getCodec().readTree(parser)
        val prices = node["prices"] as ArrayNode

        return HistoryResponse(prices.map {
            val price = it as ArrayNode
            val date = Date(price[0].asLong())
            val rate = price[1].asDouble().toBigDecimal()
            date to rate
        }.toList())
    }
}