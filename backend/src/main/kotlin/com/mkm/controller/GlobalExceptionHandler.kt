package com.mkm.controller

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {
    @ExceptionHandler(IllegalArgumentException::class)
    fun badRequest(ex: IllegalArgumentException) = error(HttpStatus.BAD_REQUEST, ex.message ?: "Bad request")

    @ExceptionHandler(IllegalStateException::class)
    fun forbidden(ex: IllegalStateException) = error(HttpStatus.FORBIDDEN, ex.message ?: "Forbidden")

    @ExceptionHandler(NoSuchElementException::class, UsernameNotFoundException::class)
    fun notFound(ex: RuntimeException) = error(HttpStatus.NOT_FOUND, ex.message ?: "Not found")

    @ExceptionHandler(BadCredentialsException::class)
    fun unauthorized() = error(HttpStatus.UNAUTHORIZED, "Invalid username or password")

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun validation(ex: MethodArgumentNotValidException): ResponseEntity<Map<String, String>> {
        val message = ex.bindingResult.fieldErrors.firstOrNull()?.defaultMessage ?: "Validation failed"
        return error(HttpStatus.BAD_REQUEST, message)
    }

    private fun error(status: HttpStatus, message: String): ResponseEntity<Map<String, String>> =
        ResponseEntity.status(status).body(mapOf("error" to message))
}
