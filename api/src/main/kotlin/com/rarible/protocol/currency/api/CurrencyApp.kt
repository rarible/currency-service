package com.rarible.protocol.currency.api

import com.rarible.core.mongo.configuration.EnableRaribleMongo
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.mongodb.config.EnableMongoAuditing
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories

@SpringBootApplication
@EnableReactiveMongoRepositories
@EnableMongoAuditing
@EnableRaribleMongo
class CurrencyApp

fun main(args: Array<String>) {
    runApplication<CurrencyApp>(*args)
}
