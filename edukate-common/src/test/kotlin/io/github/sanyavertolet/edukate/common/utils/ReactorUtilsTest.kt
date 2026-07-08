package io.github.sanyavertolet.edukate.common.utils

import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import reactor.core.publisher.Mono
import reactor.test.StepVerifier

class ReactorUtilsTest {

    private fun assertStatus(mono: Mono<*>, status: HttpStatus) =
        StepVerifier.create(mono)
            .expectErrorMatches { it is ResponseStatusException && it.statusCode.value() == status.value() }
            .verify()

    // region orThrow

    @Test
    fun `orThrow passes a present value through`() {
        StepVerifier.create(Mono.just(1).orThrow(HttpStatus.NOT_FOUND, "missing")).expectNext(1).verifyComplete()
    }

    @Test
    fun `orThrow errors on empty source`() {
        assertStatus(Mono.empty<Int>().orThrow(HttpStatus.NOT_FOUND, "missing"), HttpStatus.NOT_FOUND)
    }

    // endregion

    // region throwIf

    @Test
    fun `throwIf errors when a present value matches the condition`() {
        assertStatus(Mono.just(5).throwIf(HttpStatus.BAD_REQUEST, "too big") { it > 3 }, HttpStatus.BAD_REQUEST)
    }

    @Test
    fun `throwIf passes value through when the condition is false`() {
        StepVerifier.create(Mono.just(2).throwIf(HttpStatus.BAD_REQUEST, "too big") { it > 3 })
            .expectNext(2)
            .verifyComplete()
    }

    @Test
    fun `throwIf passes an empty source through without error`() {
        // The condition never applies to a missing value, so absence must not throw.
        StepVerifier.create(Mono.empty<Int>().throwIf(HttpStatus.BAD_REQUEST, "too big") { it > 3 }).verifyComplete()
    }

    // endregion

    // region semantic aliases

    @Test
    fun `orNotFound and orForbidden map to their statuses on empty`() {
        assertStatus(Mono.empty<Int>().orNotFound("nope"), HttpStatus.NOT_FOUND)
        assertStatus(Mono.empty<Int>().orForbidden("nope"), HttpStatus.FORBIDDEN)
    }

    @Test
    fun `notFoundIf forbiddenIf and badRequestIf throw their statuses when condition holds`() {
        assertStatus(Mono.just(1).notFoundIf("nf") { it == 1 }, HttpStatus.NOT_FOUND)
        assertStatus(Mono.just(1).forbiddenIf("fb") { it == 1 }, HttpStatus.FORBIDDEN)
        assertStatus(Mono.just(1).badRequestIf("br") { it == 1 }, HttpStatus.BAD_REQUEST)
    }

    @Test
    fun `condition aliases pass a non-matching present value through`() {
        StepVerifier.create(Mono.just(1).forbiddenIf("fb") { it == 2 }).expectNext(1).verifyComplete()
    }

    @Test
    fun `condition aliases pass an empty source through`() {
        StepVerifier.create(Mono.empty<Int>().badRequestIf("br") { it == 1 }).verifyComplete()
    }

    // endregion
}
