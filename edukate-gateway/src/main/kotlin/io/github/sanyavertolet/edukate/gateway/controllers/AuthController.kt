package io.github.sanyavertolet.edukate.gateway.controllers

import io.github.sanyavertolet.edukate.auth.services.AuthCookieService
import io.github.sanyavertolet.edukate.gateway.dtos.SignInRequest
import io.github.sanyavertolet.edukate.gateway.dtos.SignUpRequest
import io.github.sanyavertolet.edukate.gateway.services.AuthService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirements
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import reactor.core.publisher.Mono

@RestController
@Validated
@SecurityRequirements
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "API for user authentication operations")
class AuthController(private val authService: AuthService, private val authCookieService: AuthCookieService) {
    @PostMapping("/sign-in")
    @Operation(
        operationId = "sign-in",
        summary = "Sign user in",
        description = "Authenticates a user with provided credentials and returns a JWT token in a cookie",
    )
    @ApiResponses(
        value =
            [
                ApiResponse(responseCode = "204", description = "Successfully authenticated"),
                ApiResponse(responseCode = "403", description = "Authentication failed"),
            ]
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
        required = true,
        content =
            [Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = Schema(implementation = SignInRequest::class))],
    )
    fun signIn(@RequestBody @Valid signInRequest: SignInRequest): Mono<ResponseEntity<Void>> =
        authService
            .signIn(signInRequest)
            .flatMap(authCookieService::respondWithToken)
            .switchIfEmpty(Mono.error(ResponseStatusException(HttpStatus.FORBIDDEN)))

    @PostMapping("/sign-up")
    @Operation(
        operationId = "sign-up",
        summary = "Register new user",
        description = "Registers a new user with provided information and returns a JWT token in a cookie",
    )
    @ApiResponses(
        value =
            [
                ApiResponse(responseCode = "204", description = "Successfully registered"),
                ApiResponse(responseCode = "403", description = "Registration failed"),
                ApiResponse(responseCode = "409", description = "User with this name already exists"),
            ]
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
        required = true,
        content =
            [Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = Schema(implementation = SignUpRequest::class))],
    )
    fun signUp(@RequestBody @Valid signUpRequest: SignUpRequest): Mono<ResponseEntity<Void>> =
        authService
            .signUp(signUpRequest)
            .flatMap(authCookieService::respondWithToken)
            .switchIfEmpty(Mono.error(ResponseStatusException(HttpStatus.FORBIDDEN)))

    @PostMapping("/sign-out")
    @Operation(
        operationId = "sign-out",
        summary = "Sign user out",
        description = "Signs out the current user by expiring their authentication token",
    )
    @ApiResponses(
        value =
            [
                ApiResponse(responseCode = "204", description = "Successfully signed out"),
                ApiResponse(responseCode = "400", description = "Bad request"),
            ]
    )
    fun signOut(): Mono<ResponseEntity<Void>> = authCookieService.respondWithExpiredToken()
}
