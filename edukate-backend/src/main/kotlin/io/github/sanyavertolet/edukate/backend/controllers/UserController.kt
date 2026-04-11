package io.github.sanyavertolet.edukate.backend.controllers

import io.github.sanyavertolet.edukate.backend.dtos.UserDto
import io.github.sanyavertolet.edukate.backend.services.UserService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "Users", description = "API for managing user information")
@SecurityRequirement(name = "cookieAuth")
class UserController(private val userService: UserService) {
    @GetMapping("/whoami")
    @Operation(
        operationId = "whoami",
        summary = "Get current user information",
        description = "Retrieves information about the currently authenticated user",
    )
    @ApiResponses(
        value =
            [
                ApiResponse(responseCode = "200", description = "Successfully retrieved user information"),
                ApiResponse(responseCode = "401", description = "Unauthorized"),
                ApiResponse(responseCode = "404", description = "User not found by name from token"),
            ]
    )
    fun whoami(authentication: Authentication): Mono<UserDto> =
        authentication.toMono().flatMap { userService.findUserByName(it.name) }.map { UserDto.of(it) }
}
