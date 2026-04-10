package io.github.sanyavertolet.edukate.checker.services

import io.github.sanyavertolet.edukate.common.checks.CheckResultMessage
import reactor.core.publisher.Mono

fun interface ResultPublisher {
    fun publish(result: CheckResultMessage): Mono<Void>
}
