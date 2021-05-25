package com.rarible.protocol.currency.api

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.mongodb.config.EnableMongoAuditing
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories
import ru.roborox.reactive.persist.configuration.EnableRoboroxMongo

@SpringBootApplication
@EnableReactiveMongoRepositories
@EnableMongoAuditing
@EnableRoboroxMongo
class CurrencyApp

fun main(args: Array<String>) {
    runApplication<CurrencyApp>(*args)
}
