package com.rarible.protocol.currency.api.gecko

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class HistoryResponseTest {

    @Test
    fun `should read history response`() {
        val json = """
            {
              "prices": [
                [
                  1617779003234,
                  2091.855473053653
                ],
                [
                  1617779443511,
                  2079.199458673818
                ]
              ]
            }
        """.trimIndent()

        val mapper = ObjectMapper().registerModule(KotlinModule())

        val value = mapper.readValue(json, HistoryResponse::class.java)

        assertEquals(value.prices[0].second, BigDecimal.valueOf(2091.855473053653))
    }
}