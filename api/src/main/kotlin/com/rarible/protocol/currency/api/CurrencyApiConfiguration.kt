package com.rarible.protocol.currency.api

import com.rarible.core.telemetry.actuator.WebRequestClientTagContributor
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class CurrencyApiConfiguration {

    @Bean
    fun webRequestClientTagContributor(): WebRequestClientTagContributor {
        return WebRequestClientTagContributor()
    }

}