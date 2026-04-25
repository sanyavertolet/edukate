package io.github.sanyavertolet.edukate.backend.controllers

import io.github.sanyavertolet.edukate.backend.dtos.UserDto
import io.github.sanyavertolet.edukate.backend.services.ProblemSetService
import io.github.sanyavertolet.edukate.backend.services.UserService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.constraints.Positive
import org.springframework.security.core.Authentication
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

@RestController
@Validated
@RequestMapping("/api/v1/users")
@Tag(name = "Users", description = "API for managing user information")
@SecurityRequirement(name = "cookieAuth")
class UserController(private val userService: UserService, private val problemSetService: ProblemSetService) {
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

    @GetMapping("/by-prefix")
    @Operation(
        summary = "Get usernames by prefix",
        description =
            "Retrieves a list of usernames matching the given prefix, ordered alphabetically. " +
                "When problemSetShareCode is provided, excludes users who are already members or invited.",
    )
    @ApiResponses(
        value =
            [
                ApiResponse(responseCode = "200", description = "Successfully retrieved usernames"),
                ApiResponse(responseCode = "400", description = "Validation failed"),
            ]
    )
    @io.swagger.v3.oas.annotations.Parameters(
        value =
            [
                Parameter(
                    name = "prefix",
                    description = "The prefix to match usernames against",
                    `in` = ParameterIn.QUERY,
                    required = true,
                ),
                Parameter(name = "limit", description = "Maximum number of results to return", `in` = ParameterIn.QUERY),
                Parameter(
                    name = "problemSetShareCode",
                    description = "If provided, excludes members and invited users of this problem set",
                    `in` = ParameterIn.QUERY,
                ),
            ]
    )
    fun getUserNamesByPrefix(
        @RequestParam prefix: String,
        @RequestParam(required = false, defaultValue = "5") @Positive limit: Int,
        @RequestParam(required = false) problemSetShareCode: String?,
    ): Mono<List<String>> =
        problemSetShareCode?.let { code ->
            problemSetService.findByShareCode(code).flatMap { ps ->
                val excludeIds = ps.userIdRoleMap.keys + ps.invitedUserIds
                userService.getUserNamesByPrefix(prefix, limit, excludeIds).collectList()
            }
        } ?: userService.getUserNamesByPrefix(prefix, limit).collectList()
}
