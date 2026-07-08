package io.github.sanyavertolet.edukate.backend.controllers

import io.github.sanyavertolet.edukate.backend.dtos.EmailChangeRequest
import io.github.sanyavertolet.edukate.backend.mappers.UserMapper
import io.github.sanyavertolet.edukate.backend.services.AuthTokenService
import io.github.sanyavertolet.edukate.backend.services.UserService
import io.github.sanyavertolet.edukate.backend.services.files.FileManager
import io.github.sanyavertolet.edukate.common.utils.id
import io.github.sanyavertolet.edukate.common.utils.orNotFound
import io.github.sanyavertolet.edukate.storage.keys.UserAvatarFileKey
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import java.nio.ByteBuffer
import java.time.Instant
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.core.Authentication
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@RestController
@Validated
@RequestMapping("/api/v1/users/me")
@Tag(name = "User Settings", description = "API for managing the current user's profile and credentials")
@SecurityRequirement(name = "cookieAuth")
class UserSettingsController(
    private val userService: UserService,
    private val fileManager: FileManager,
    private val authTokenService: AuthTokenService,
    private val userMapper: UserMapper,
) {
    @PutMapping("/avatar", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE], produces = [MediaType.TEXT_PLAIN_VALUE])
    @Operation(summary = "Upload or replace the current user's avatar")
    @ApiResponses(
        value =
            [
                ApiResponse(
                    responseCode = "200",
                    description = "Avatar uploaded; body is the new public URL",
                    content = [Content(schema = Schema(implementation = String::class))],
                ),
                ApiResponse(responseCode = "401", description = "Unauthorized", content = [Content()]),
            ]
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
        required = true,
        content =
            [
                Content(
                    mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE,
                    schema = Schema(type = "string", format = "binary"),
                )
            ],
    )
    fun uploadAvatar(@RequestPart("content") content: Flux<ByteBuffer>, authentication: Authentication): Mono<String> {
        val userId = requireNotNull(authentication.id())
        val key = UserAvatarFileKey(userId)
        return fileManager
            .uploadFile(key, MediaType.IMAGE_JPEG, content)
            .then(userService.setAvatarTimestamp(userId, Instant.now()))
            .map { user -> requireNotNull(userMapper.avatarUrl(user)) }
    }

    @DeleteMapping("/avatar")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Remove the current user's avatar")
    @ApiResponses(
        value =
            [
                ApiResponse(responseCode = "204", description = "Avatar removed"),
                ApiResponse(responseCode = "401", description = "Unauthorized", content = [Content()]),
            ]
    )
    fun deleteAvatar(authentication: Authentication): Mono<Void> {
        val userId = requireNotNull(authentication.id())
        val key = UserAvatarFileKey(userId)
        return fileManager.deleteFile(key).then(userService.setAvatarTimestamp(userId, null)).then()
    }

    @PostMapping("/email-change")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @Operation(summary = "Request an email change — sends a verification link to the new address")
    @ApiResponses(
        value =
            [
                ApiResponse(responseCode = "202", description = "Verification email dispatched"),
                ApiResponse(responseCode = "400", description = "Invalid email", content = [Content()]),
                ApiResponse(responseCode = "401", description = "Unauthorized", content = [Content()]),
                ApiResponse(responseCode = "409", description = "Email already in use", content = [Content()]),
            ]
    )
    fun requestEmailChange(@Valid @RequestBody body: EmailChangeRequest, authentication: Authentication): Mono<Void> {
        val userId = requireNotNull(authentication.id())
        return userService.findUserById(userId).orNotFound("User not found").flatMap {
            authTokenService.issueVerificationToken(userId, body.newEmail)
        }
    }
}
