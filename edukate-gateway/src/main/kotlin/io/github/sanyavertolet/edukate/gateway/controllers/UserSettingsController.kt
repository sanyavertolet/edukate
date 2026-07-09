package io.github.sanyavertolet.edukate.gateway.controllers

import io.github.sanyavertolet.edukate.auth.services.AuthCookieService
import io.github.sanyavertolet.edukate.common.utils.id
import io.github.sanyavertolet.edukate.gateway.dtos.ChangePasswordRequest
import io.github.sanyavertolet.edukate.gateway.dtos.ChangeUsernameRequest
import io.github.sanyavertolet.edukate.gateway.services.AuthService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import reactor.core.publisher.Mono

@RestController
@Validated
@RequestMapping("/api/v1/users/me")
@Tag(name = "User Settings", description = "API for changing the current user's credentials")
@SecurityRequirement(name = "cookieAuth")
class UserSettingsController(private val authService: AuthService, private val authCookieService: AuthCookieService) {
    @PatchMapping("/username")
    @Operation(
        operationId = "change-username",
        summary = "Change current user's username",
        description = "Renames the current user and re-issues the JWT cookie so the session reflects the new name",
    )
    @ApiResponses(
        value =
            [
                ApiResponse(responseCode = "204", description = "Username updated; cookie reissued"),
                ApiResponse(responseCode = "401", description = "Unauthorized"),
                ApiResponse(responseCode = "409", description = "Username already taken"),
            ]
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
        required = true,
        content =
            [
                Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = Schema(implementation = ChangeUsernameRequest::class),
                )
            ],
    )
    fun changeUsername(
        @RequestBody @Valid request: ChangeUsernameRequest,
        authentication: Authentication,
    ): Mono<ResponseEntity<Void>> {
        val userId = requireNotNull(authentication.id())
        return authService.changeUsername(userId, request.newUsername).flatMap(authCookieService::respondWithToken)
    }

    @PostMapping("/password")
    @Operation(
        operationId = "change-password",
        summary = "Change current user's password",
        description = "Verifies the current password and stores the new one. JWT cookie is NOT reissued.",
    )
    @ApiResponses(
        value =
            [
                ApiResponse(responseCode = "204", description = "Password updated"),
                ApiResponse(responseCode = "400", description = "Current password is incorrect"),
                ApiResponse(responseCode = "401", description = "Unauthorized"),
            ]
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
        required = true,
        content =
            [
                Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = Schema(implementation = ChangePasswordRequest::class),
                )
            ],
    )
    fun changePassword(
        @RequestBody @Valid request: ChangePasswordRequest,
        authentication: Authentication,
    ): Mono<ResponseEntity<Void>> {
        if (request.currentPassword == request.newPassword) {
            return Mono.error(
                ResponseStatusException(HttpStatus.BAD_REQUEST, "New password must differ from current password")
            )
        }
        return authService
            .changePassword(authentication.name, request.currentPassword, request.newPassword)
            .thenReturn(ResponseEntity.noContent().build())
    }
}
