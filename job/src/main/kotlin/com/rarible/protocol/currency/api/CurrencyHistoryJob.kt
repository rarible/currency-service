package com.rarible.protocol.currency.api

import com.github.cloudyrock.spring.v5.EnableMongock
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.mongodb.config.EnableMongoAuditing
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableReactiveMongoRepositories
@EnableScheduling
@EnableMongoAuditing
@EnableMongock
class CurrencyHistoryJob

fun main(args: Array<String>) {
    runApplication<CurrencyHistoryJob>(*args)
}
