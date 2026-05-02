package com.quietchatter.member.adaptor.`in`.web.error

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

@RestControllerAdvice
class GlobalExceptionHandler : ResponseEntityExceptionHandler() {
    private val log = LoggerFactory.getLogger(javaClass)

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(ex: IllegalArgumentException): ProblemDetail {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.message ?: "Invalid request")
    }

    @ExceptionHandler(NoSuchElementException::class)
    fun handleNoSuchElementException(ex: NoSuchElementException): ProblemDetail {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.message ?: "Resource not found")
    }

    @ExceptionHandler(Exception::class)
    fun handleAllExceptions(ex: Exception): ProblemDetail {
        log.error("Unhandled exception occurred", ex)
        return ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "An internal server error occurred")
    }
}
