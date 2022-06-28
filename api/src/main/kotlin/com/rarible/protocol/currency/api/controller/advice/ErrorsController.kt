package com.rarible.protocol.currency.api.controller.advice

import com.rarible.protocol.currency.core.exceptions.CurrencyApiException
import com.rarible.protocol.currency.dto.CurrencyApiErrorDto
import kotlinx.coroutines.reactor.mono
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice(basePackages = ["com.rarible.protocol.currency.api.controller"])
class ErrorsController {
    private val logger = LoggerFactory.getLogger(this::class.java)

    @ExceptionHandler(CurrencyApiException::class)
    fun handleIndexerApiException(ex: CurrencyApiException) = mono {
        logWithNecessaryLevel(ex.status, ex, "Template api error while handle request")

        val error = CurrencyApiErrorDto(
            status = ex.status.value(),
            code = ex.code,
            message = ex.message ?: "Missing message in error"
        )
        ResponseEntity.status(ex.status).body(error)
    }

    @ExceptionHandler(Throwable::class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    fun handlerException(ex: Throwable) = mono {
        logger.error("System error while handling request", ex)

        CurrencyApiErrorDto(
            status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
            code = CurrencyApiErrorDto.Code.UNKNOWN,
            message = ex.message ?: "Something went wrong"
        )
    }

    private fun logWithNecessaryLevel(status: HttpStatus, ex: Exception, message: String = "") {
        if (status.is5xxServerError) {
            logger.error(message, ex)
        } else {
            logger.warn(message, ex)
        }
    }
}
