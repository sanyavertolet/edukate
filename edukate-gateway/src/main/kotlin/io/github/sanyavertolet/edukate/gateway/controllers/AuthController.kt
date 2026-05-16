package io.github.sanyavertolet.edukate.gateway.controllers

import io.github.sanyavertolet.edukate.auth.services.AuthCookieService
import io.github.sanyavertolet.edukate.gateway.dtos.ForgotPasswordRequest
import io.github.sanyavertolet.edukate.gateway.dtos.ResetPasswordRequest
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
import java.net.URI
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import reactor.core.publisher.Mono

@RestController
@Validated
@SecurityRequirements
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "API for user authentication operations")
class AuthController(
    private val authService: AuthService,
    private val authCookieService: AuthCookieService,
    @param:Value($$"${app.url}") private val appUrl: String,
) {
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
        description = "Registers a new user and sends a verification email. The account must be verified before signing in.",
    )
    @ApiResponses(
        value =
            [
                ApiResponse(responseCode = "202", description = "Registration accepted — verification email sent"),
                ApiResponse(responseCode = "409", description = "User with this name already exists"),
            ]
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
        required = true,
        content =
            [Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = Schema(implementation = SignUpRequest::class))],
    )
    fun signUp(@RequestBody @Valid signUpRequest: SignUpRequest): Mono<ResponseEntity<Void>> =
        authService.signUp(signUpRequest).thenReturn(ResponseEntity.accepted().build())

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

    @GetMapping("/verify-email")
    @Operation(
        operationId = "verify-email",
        summary = "Verify email address",
        description = "Validates the one-time token from the verification email and activates the account",
    )
    @ApiResponses(
        value =
            [
                ApiResponse(responseCode = "302", description = "Email verified — redirecting to sign-in"),
                ApiResponse(responseCode = "404", description = "Token not found"),
                ApiResponse(responseCode = "410", description = "Token expired"),
            ]
    )
    fun verifyEmail(@RequestParam token: String): Mono<ResponseEntity<Void>> =
        authService
            .verifyEmail(token)
            .thenReturn(
                ResponseEntity.status(HttpStatus.FOUND).location(URI.create("$appUrl/sign-in?verified=true")).build()
            )

    @PostMapping("/forgot-password")
    @Operation(
        operationId = "forgot-password",
        summary = "Request password reset",
        description =
            "Sends a password reset link to the given email address if an account exists." +
                " Always returns 202 to prevent email enumeration.",
    )
    @ApiResponses(value = [ApiResponse(responseCode = "202", description = "Reset link sent if account exists")])
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
        required = true,
        content =
            [
                Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = Schema(implementation = ForgotPasswordRequest::class),
                )
            ],
    )
    fun forgotPassword(@RequestBody @Valid request: ForgotPasswordRequest): Mono<ResponseEntity<Void>> =
        authService.forgotPassword(request.email).thenReturn(ResponseEntity.accepted().build())

    @PostMapping("/reset-password")
    @Operation(
        operationId = "reset-password",
        summary = "Reset password",
        description = "Validates the one-time reset token and updates the account password",
    )
    @ApiResponses(
        value =
            [
                ApiResponse(responseCode = "204", description = "Password updated successfully"),
                ApiResponse(responseCode = "400", description = "Validation failed"),
                ApiResponse(responseCode = "404", description = "Token not found"),
                ApiResponse(responseCode = "410", description = "Token expired"),
            ]
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
        required = true,
        content =
            [
                Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = Schema(implementation = ResetPasswordRequest::class),
                )
            ],
    )
    fun resetPassword(@RequestBody @Valid request: ResetPasswordRequest): Mono<ResponseEntity<Void>> =
        authService.resetPassword(request.token, request.newPassword).thenReturn(ResponseEntity.noContent().build())
}
