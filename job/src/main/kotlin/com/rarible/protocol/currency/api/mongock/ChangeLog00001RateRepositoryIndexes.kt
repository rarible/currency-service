package com.rarible.protocol.currency.api.mongock

import com.github.cloudyrock.mongock.ChangeLog
import com.github.cloudyrock.mongock.ChangeSet
import com.rarible.protocol.currency.core.repository.RateRepository
import io.changock.migration.api.annotations.NonLockGuarded
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory

@ChangeLog(order = "00001")
class ChangeLog00001RateRepositoryIndexes {
    private val logger = LoggerFactory.getLogger(this::class.java)

    @ChangeSet(id = "ChangeLog00001RateRepositoryIndexes.createIndexes", order = "1", author = "protocol")
    fun createHistoryIndexes(@NonLockGuarded rateRepository: RateRepository) = runBlocking {
        rateRepository.createIndexes()
    }
}