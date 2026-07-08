package io.github.sanyavertolet.edukate.common.utils

import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import reactor.core.publisher.Mono

// --- base ---

fun <T : Any> Mono<T>.orThrow(status: HttpStatus, message: String): Mono<T> =
    switchIfEmpty(Mono.error(ResponseStatusException(status, message)))

/**
 * Throws [status] error if a value is present and [condition] is TRUE (the failure case). An empty source passes through
 * untouched — absence is not a failure, since the condition never applies to a missing value. Guard emptiness explicitly
 * with [orThrow] / [orNotFound] when you need it.
 */
fun <T : Any> Mono<T>.throwIf(status: HttpStatus, message: String, condition: (T) -> Boolean): Mono<T> = flatMap {
    if (condition(it)) Mono.error(ResponseStatusException(status, message)) else Mono.just(it)
}

// --- semantic aliases for common HTTP statuses ---

fun <T : Any> Mono<T>.orNotFound(message: String): Mono<T> = orThrow(HttpStatus.NOT_FOUND, message)

fun <T : Any> Mono<T>.orForbidden(message: String): Mono<T> = orThrow(HttpStatus.FORBIDDEN, message)

fun <T : Any> Mono<T>.notFoundIf(message: String, condition: (T) -> Boolean): Mono<T> =
    throwIf(HttpStatus.NOT_FOUND, message, condition)

fun <T : Any> Mono<T>.forbiddenIf(message: String, condition: (T) -> Boolean): Mono<T> =
    throwIf(HttpStatus.FORBIDDEN, message, condition)

fun <T : Any> Mono<T>.badRequestIf(message: String, condition: (T) -> Boolean): Mono<T> =
    throwIf(HttpStatus.BAD_REQUEST, message, condition)
