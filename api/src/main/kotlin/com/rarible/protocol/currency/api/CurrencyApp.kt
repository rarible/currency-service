package com.rarible.protocol.currency.api

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.mongodb.config.EnableMongoAuditing
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableReactiveMongoRepositories
@EnableScheduling
@EnableMongoAuditing
class CurrencyApp

fun main(args: Array<String>) {
    runApplication<CurrencyApp>(*args)
}
