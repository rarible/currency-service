package com.rarible.protocol.currency.api

import com.rarible.core.telemetry.actuator.WebRequestClientTagContributor
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(AbukataListProp::class)
class CurrencyApiConfiguration {

    @Bean
    fun webRequestClientTagContributor(): WebRequestClientTagContributor {
        return WebRequestClientTagContributor()
    }

}