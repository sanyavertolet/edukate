package io.github.sanyavertolet.edukate.backend.controllers.internal

import io.github.sanyavertolet.edukate.backend.services.AuthTokenService
import io.swagger.v3.oas.annotations.Hidden
import io.swagger.v3.oas.annotations.security.SecurityRequirements
import java.util.UUID
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@Hidden
@RestController
@SecurityRequirements
@RequestMapping("/internal/tokens")
class AuthTokenInternalController(private val authTokenService: AuthTokenService) {
    @PostMapping("/verification")
    fun issueVerificationToken(@RequestBody body: Map<String, String>): Mono<Void> =
        authTokenService.issueVerificationToken(
            requireNotNull(body["userId"]?.toLong()) { "userId is required" },
            requireNotNull(body["email"]) { "email is required" },
        )

    @PostMapping("/password-reset")
    fun issuePasswordResetToken(@RequestBody body: Map<String, String>): Mono<Void> =
        authTokenService.issuePasswordResetToken(requireNotNull(body["email"]) { "email is required" })

    @PostMapping("/consume/verification")
    fun consumeVerificationToken(@RequestBody body: Map<String, String>): Mono<Void> =
        authTokenService.consumeVerificationToken(UUID.fromString(requireNotNull(body["token"]) { "token is required" }))

    @PostMapping("/consume/reset")
    fun consumeResetToken(@RequestBody body: Map<String, String>): Mono<Void> =
        authTokenService.consumeResetToken(
            UUID.fromString(requireNotNull(body["token"]) { "token is required" }),
            requireNotNull(body["encodedPassword"]) { "encodedPassword is required" },
        )
}
