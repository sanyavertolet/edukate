package io.github.sanyavertolet.edukate.backend.controllers

import io.github.sanyavertolet.edukate.backend.dtos.ChangeProblemSetProblemsRequest
import io.github.sanyavertolet.edukate.backend.dtos.CreateProblemSetRequest
import io.github.sanyavertolet.edukate.backend.dtos.ProblemSetDto
import io.github.sanyavertolet.edukate.backend.dtos.ProblemSetMetadata
import io.github.sanyavertolet.edukate.backend.dtos.UserNameWithRole
import io.github.sanyavertolet.edukate.backend.mappers.ProblemSetMapper
import io.github.sanyavertolet.edukate.backend.permissions.ProblemSetPermissionEvaluator
import io.github.sanyavertolet.edukate.backend.services.ProblemSetService
import io.github.sanyavertolet.edukate.backend.services.UserService
import io.github.sanyavertolet.edukate.common.notifications.InviteNotificationCreateRequest
import io.github.sanyavertolet.edukate.common.services.Notifier
import io.github.sanyavertolet.edukate.common.users.UserRole
import io.github.sanyavertolet.edukate.common.utils.id
import io.github.sanyavertolet.edukate.common.utils.monoId
import io.github.sanyavertolet.edukate.common.utils.orForbidden
import io.github.sanyavertolet.edukate.common.utils.orNotFound
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.security.SecurityRequirements
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.PositiveOrZero
import org.springframework.data.domain.PageRequest
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@RestController
@Validated
@RequestMapping("/api/v1/problem-sets")
@Tag(name = "Problem Sets", description = "API for managing problem sets and user collaborations")
@Suppress("TooManyFunctions")
class ProblemSetController(
    private val problemSetService: ProblemSetService,
    private val problemSetMapper: ProblemSetMapper,
    private val userService: UserService,
    private val notifier: Notifier,
    private val problemSetPermissionEvaluator: ProblemSetPermissionEvaluator,
) {
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "cookieAuth")
    @Operation(summary = "Create problem set", description = "Creates a new problem set for the authenticated user")
    @ApiResponses(
        value =
            [
                ApiResponse(responseCode = "200", description = "Problem set created"),
                ApiResponse(responseCode = "400", description = "Validation failed"),
                ApiResponse(responseCode = "401", description = "Unauthorized"),
            ]
    )
    fun createProblemSet(
        @RequestBody @Valid request: CreateProblemSetRequest,
        authentication: Authentication,
    ): Mono<ProblemSetDto> =
        problemSetService.createProblemSet(request, authentication).flatMap { problemSetMapper.toDto(it, authentication) }

    @GetMapping("/owned")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "cookieAuth")
    @Operation(
        summary = "Get owned problem sets",
        description = "Returns a paginated list of problem sets owned by the authenticated user",
    )
    @ApiResponses(
        value =
            [
                ApiResponse(responseCode = "200", description = "Successfully retrieved owned problem sets"),
                ApiResponse(responseCode = "400", description = "Validation failed"),
                ApiResponse(responseCode = "401", description = "Unauthorized"),
            ]
    )
    fun getOwnedProblemSets(
        @RequestParam(defaultValue = "0") @PositiveOrZero page: Int,
        @RequestParam(defaultValue = "10") @Positive size: Int,
        authentication: Authentication,
    ): Flux<ProblemSetMetadata> =
        problemSetService.getOwnedProblemSets(PageRequest.of(page, size), authentication).flatMap {
            problemSetMapper.toMetadata(it, authentication)
        }

    @GetMapping("/joined")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "cookieAuth")
    @Operation(
        summary = "Get joined problem sets",
        description = "Returns a paginated list of problem sets the authenticated user has joined",
    )
    @ApiResponses(
        value =
            [
                ApiResponse(responseCode = "200", description = "Successfully retrieved joined problem sets"),
                ApiResponse(responseCode = "400", description = "Validation failed"),
                ApiResponse(responseCode = "401", description = "Unauthorized"),
            ]
    )
    fun getJoinedProblemSets(
        @RequestParam(defaultValue = "0") @PositiveOrZero page: Int,
        @RequestParam(defaultValue = "10") @Positive size: Int,
        authentication: Authentication,
    ): Flux<ProblemSetMetadata> =
        problemSetService.getJoinedProblemSets(PageRequest.of(page, size), authentication).flatMap {
            problemSetMapper.toMetadata(it, authentication)
        }

    @GetMapping("/public")
    @SecurityRequirements
    @Operation(
        summary = "Get public problem sets",
        description = "Returns a paginated list of publicly visible problem sets",
    )
    @ApiResponses(
        value =
            [
                ApiResponse(responseCode = "200", description = "Successfully retrieved public problem sets"),
                ApiResponse(responseCode = "400", description = "Validation failed"),
            ]
    )
    fun getPublicProblemSets(
        @RequestParam(defaultValue = "0") @PositiveOrZero page: Int,
        @RequestParam(defaultValue = "10") @Positive size: Int,
        authentication: Authentication?,
    ): Flux<ProblemSetMetadata> =
        problemSetService.getPublicProblemSets(PageRequest.of(page, size)).flatMap {
            problemSetMapper.toMetadata(it, authentication)
        }

    @GetMapping("/{shareCode}")
    @SecurityRequirements
    @Operation(
        summary = "Get problem set by share code",
        description =
            "Retrieves a problem set by its share code; returns 403 if the set is private and the caller is not a member",
    )
    @ApiResponses(
        value =
            [
                ApiResponse(responseCode = "200", description = "Successfully retrieved problem set"),
                ApiResponse(responseCode = "403", description = "Problem set is private and caller is not a member"),
                ApiResponse(responseCode = "404", description = "Problem set not found"),
            ]
    )
    fun getProblemSetByShareCode(
        @PathVariable @NotBlank shareCode: String,
        authentication: Authentication?,
    ): Mono<ProblemSetDto> =
        problemSetService
            .findByShareCode(shareCode)
            .filter { problemSetPermissionEvaluator.hasReadPermission(it, authentication?.id()) }
            .orForbidden("Problem set is private and you are not a member.")
            .flatMap { problemSetMapper.toDto(it, authentication) }

    @PostMapping("/{shareCode}/leave")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "cookieAuth")
    @Operation(summary = "Leave problem set", description = "Removes the authenticated user from the problem set")
    @ApiResponses(
        value =
            [
                ApiResponse(responseCode = "200", description = "Successfully left problem set"),
                ApiResponse(responseCode = "401", description = "Unauthorized"),
                ApiResponse(responseCode = "404", description = "Problem set not found or user is not a member"),
            ]
    )
    fun leaveProblemSet(@PathVariable @NotBlank shareCode: String, authentication: Authentication): Mono<String> =
        authentication.monoId().flatMap { problemSetService.removeUser(shareCode, it) }.map { it.shareCode }

    @PostMapping("/{shareCode}/invite")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "cookieAuth")
    @Operation(summary = "Invite user to problem set", description = "Sends an invitation to a user to join the problem set")
    @ApiResponses(
        value =
            [
                ApiResponse(responseCode = "200", description = "Invitation sent"),
                ApiResponse(responseCode = "401", description = "Unauthorized"),
                ApiResponse(responseCode = "403", description = "Caller does not have moderator access"),
                ApiResponse(responseCode = "404", description = "Problem set or invitee not found"),
            ]
    )
    fun inviteToProblemSet(
        @PathVariable @NotBlank shareCode: String,
        @RequestParam @NotBlank inviteeName: String,
        authentication: Authentication,
    ): Mono<String> =
        userService
            .findUserByName(inviteeName)
            .orNotFound("User $inviteeName not found")
            .zipWhen(
                { invitee ->
                    val requesterId = requireNotNull(authentication.id())
                    problemSetService.inviteUser(shareCode, requesterId, requireNotNull(invitee.id))
                },
                { invitee, ps ->
                    InviteNotificationCreateRequest.from(
                        targetUserId = requireNotNull(invitee.id),
                        inviterName = authentication.name,
                        problemSetName = ps.name,
                        problemSetShareCode = ps.shareCode,
                    )
                },
            )
            .flatMap { notifier.notify(it) }
            .thenReturn("User $inviteeName has been invited to problem set $shareCode")

    @PostMapping("/{shareCode}/expire-invite")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "cookieAuth")
    @Operation(
        summary = "Expire invitation",
        description = "Cancels a pending invitation for a user to join the problem set",
    )
    @ApiResponses(
        value =
            [
                ApiResponse(responseCode = "200", description = "Invitation expired"),
                ApiResponse(responseCode = "401", description = "Unauthorized"),
                ApiResponse(responseCode = "403", description = "Caller does not have moderator access"),
                ApiResponse(responseCode = "404", description = "Problem set or invitee not found"),
            ]
    )
    fun expireInvite(
        @PathVariable @NotBlank shareCode: String,
        @RequestParam @NotBlank inviteeName: String,
        authentication: Authentication,
    ): Mono<String> =
        userService
            .findUserByName(inviteeName)
            .orNotFound("User $inviteeName not found")
            .flatMap { invitee ->
                val requesterId = requireNotNull(authentication.id())
                problemSetService.expireInvite(shareCode, requesterId, requireNotNull(invitee.id))
            }
            .thenReturn("Invitation for user $inviteeName has been expired in problem set $shareCode")

    @PostMapping("/{shareCode}/reply-invite")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "cookieAuth")
    @Operation(
        summary = "Reply to invitation",
        description = "Accepts or declines a pending invitation to join the problem set",
    )
    @ApiResponses(
        value =
            [
                ApiResponse(responseCode = "200", description = "Invitation reply recorded"),
                ApiResponse(responseCode = "401", description = "Unauthorized"),
                ApiResponse(responseCode = "404", description = "Problem set not found or no pending invitation"),
            ]
    )
    fun replyToInvite(
        @PathVariable @NotBlank shareCode: String,
        @RequestParam @NotNull response: Boolean,
        authentication: Authentication,
    ): Mono<String> =
        problemSetService
            .reactToInvite(shareCode, response, authentication)
            .thenReturn(
                if (response) "You have accepted invite to problem set $shareCode"
                else "You have declined invite to problem set $shareCode"
            )

    @GetMapping("/{shareCode}/users")
    @SecurityRequirement(name = "cookieAuth")
    @Operation(
        summary = "Get user roles",
        description = "Returns all users and their roles in the problem set; requires moderator access",
    )
    @ApiResponses(
        value =
            [
                ApiResponse(responseCode = "200", description = "Successfully retrieved user roles"),
                ApiResponse(responseCode = "401", description = "Unauthorized"),
                ApiResponse(responseCode = "403", description = "Caller does not have moderator access"),
            ]
    )
    fun getUserRoles(@PathVariable @NotBlank shareCode: String, authentication: Authentication): Flux<UserNameWithRole> =
        problemSetService.getProblemSetForModerator(shareCode, authentication).flatMapMany {
            problemSetMapper.toUserRoles(it)
        }

    @GetMapping("/{shareCode}/invited-users")
    @SecurityRequirement(name = "cookieAuth")
    @Operation(
        summary = "Get invited users",
        description = "Returns the list of users with pending invitations to the problem set; requires moderator access",
    )
    @ApiResponses(
        value =
            [
                ApiResponse(responseCode = "200", description = "Successfully retrieved invited users"),
                ApiResponse(responseCode = "401", description = "Unauthorized"),
                ApiResponse(responseCode = "403", description = "Caller does not have moderator access"),
            ]
    )
    fun getInvitedUsers(@PathVariable @NotBlank shareCode: String, authentication: Authentication): Mono<List<String>> =
        problemSetService
            .getProblemSetForModerator(shareCode, authentication)
            .flatMapMany { problemSetMapper.toInvitedUserNames(it) }
            .collectList()

    @PostMapping("/{shareCode}/role")
    @SecurityRequirement(name = "cookieAuth")
    @Operation(
        summary = "Change or remove user role",
        description =
            "Updates the role of a user within the problem set, or removes the user if role is not provided. " +
                "Requires moderator access and a higher role than the target user.",
    )
    @ApiResponses(
        value =
            [
                ApiResponse(responseCode = "200", description = "Role updated or user removed"),
                ApiResponse(responseCode = "401", description = "Unauthorized"),
                ApiResponse(responseCode = "403", description = "Caller does not have sufficient permissions"),
                ApiResponse(responseCode = "404", description = "Problem set or user not found"),
            ]
    )
    fun changeUserRole(
        @PathVariable @NotBlank shareCode: String,
        @RequestParam @NotBlank username: String,
        @RequestParam(required = false) requestedRole: UserRole?,
        authentication: Authentication,
    ): Mono<UserRole> =
        userService
            .findUserByName(username)
            .orNotFound("User $username not found")
            .mapNotNull { it.id }
            .flatMap { userId ->
                requestedRole?.let { problemSetService.changeUserRole(shareCode, userId, it, authentication) }
                    ?: problemSetService.removeUserByModerator(shareCode, userId, authentication).then(Mono.empty())
            }

    @PostMapping("/{shareCode}/visibility")
    @SecurityRequirement(name = "cookieAuth")
    @Operation(
        summary = "Change visibility",
        description = "Toggles the public/private visibility of the problem set; requires moderator access",
    )
    @ApiResponses(
        value =
            [
                ApiResponse(responseCode = "200", description = "Visibility updated"),
                ApiResponse(responseCode = "401", description = "Unauthorized"),
                ApiResponse(responseCode = "403", description = "Caller does not have moderator access"),
            ]
    )
    fun changeVisibility(
        @PathVariable @NotBlank shareCode: String,
        @RequestParam @NotNull isPublic: Boolean,
        authentication: Authentication,
    ): Mono<ProblemSetDto> =
        problemSetService.changeVisibility(shareCode, isPublic, authentication).flatMap {
            problemSetMapper.toDto(it, authentication)
        }

    @PostMapping("/{shareCode}/problems")
    @SecurityRequirement(name = "cookieAuth")
    @Operation(
        summary = "Change problems",
        description = "Replaces the problem list of the problem set; requires moderator access",
    )
    @ApiResponses(
        value =
            [
                ApiResponse(responseCode = "200", description = "Problems updated"),
                ApiResponse(responseCode = "400", description = "Validation failed"),
                ApiResponse(responseCode = "401", description = "Unauthorized"),
                ApiResponse(responseCode = "403", description = "Caller does not have moderator access"),
            ]
    )
    fun changeProblems(
        @PathVariable @NotBlank shareCode: String,
        @RequestBody @Valid request: ChangeProblemSetProblemsRequest,
        authentication: Authentication,
    ): Mono<ProblemSetDto> =
        problemSetService.changeProblems(shareCode, request.problemKeys, authentication).flatMap {
            problemSetMapper.toDto(it, authentication)
        }
}
