package io.github.sanyavertolet.edukate.backend.controllers

import io.github.sanyavertolet.edukate.backend.dtos.CreateInvitationRequest
import io.github.sanyavertolet.edukate.backend.dtos.CreateProblemSetRequest
import io.github.sanyavertolet.edukate.backend.dtos.ProblemSetDto
import io.github.sanyavertolet.edukate.backend.dtos.ProblemSetMetadata
import io.github.sanyavertolet.edukate.backend.dtos.SetMemberRoleRequest
import io.github.sanyavertolet.edukate.backend.dtos.UpdateProblemSetSettingsRequest
import io.github.sanyavertolet.edukate.backend.dtos.UserNameWithRole
import io.github.sanyavertolet.edukate.backend.mappers.ProblemSetMapper
import io.github.sanyavertolet.edukate.backend.permissions.ProblemSetPermissionEvaluator
import io.github.sanyavertolet.edukate.backend.services.ProblemSetService
import io.github.sanyavertolet.edukate.backend.services.UserService
import io.github.sanyavertolet.edukate.common.users.UserRole
import io.github.sanyavertolet.edukate.common.utils.id
import io.github.sanyavertolet.edukate.common.utils.orNotFound
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.Parameters
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.security.SecurityRequirements
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.PositiveOrZero
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
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
    private val problemSetPermissionEvaluator: ProblemSetPermissionEvaluator,
) {
    // region collection

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

    @GetMapping("/member")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "cookieAuth")
    @Operation(
        summary = "Get member problem sets",
        description =
            "Returns a paginated list of problem sets the authenticated user is a member of, " +
                "optionally filtered to one or more roles. When no roles are passed, all roles are included.",
    )
    @ApiResponses(
        value =
            [
                ApiResponse(responseCode = "200", description = "Successfully retrieved member problem sets"),
                ApiResponse(responseCode = "400", description = "Validation failed"),
                ApiResponse(responseCode = "401", description = "Unauthorized"),
            ]
    )
    @Parameters(
        value =
            [
                Parameter(
                    name = "roles",
                    description =
                        "Optional. Restricts results to sets where the user holds one of the given roles. " +
                            "Multi-valued: ?roles=USER&roles=MODERATOR. Omit to include every role.",
                    `in` = ParameterIn.QUERY,
                    required = false,
                    array = ArraySchema(schema = Schema(implementation = UserRole::class)),
                )
            ]
    )
    fun getMemberProblemSets(
        @RequestParam(required = false) roles: List<UserRole>?,
        @RequestParam(defaultValue = "0") @PositiveOrZero page: Int,
        @RequestParam(defaultValue = "10") @Positive size: Int,
        authentication: Authentication,
    ): Flux<ProblemSetMetadata> =
        problemSetService.getMemberProblemSets(roles, PageRequest.of(page, size), authentication).flatMap {
            problemSetMapper.toMetadata(it, authentication)
        }

    @GetMapping("/search/member")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "cookieAuth")
    @Operation(
        summary = "Search member problem sets",
        description =
            "Returns problem sets the user is a member of (any role: USER, MODERATOR, ADMIN) whose name or " +
                "share code contains the query string. Optionally filters to sets containing the given problem key.",
    )
    @ApiResponses(
        value =
            [
                ApiResponse(responseCode = "200", description = "Search results"),
                ApiResponse(responseCode = "400", description = "Validation failed"),
                ApiResponse(responseCode = "401", description = "Unauthorized"),
            ]
    )
    @Parameters(
        value =
            [
                Parameter(
                    name = "problemKey",
                    description =
                        "Optional. When provided, restricts results to problem sets containing this problem key " +
                            "(e.g. 'savchenko/1.1').",
                    `in` = ParameterIn.QUERY,
                    required = false,
                    schema = Schema(type = "string"),
                )
            ]
    )
    fun searchMemberProblemSets(
        @RequestParam(defaultValue = "") query: String,
        @RequestParam(required = false) problemKey: String?,
        @RequestParam(defaultValue = "0") @PositiveOrZero page: Int,
        @RequestParam(defaultValue = "10") @Positive size: Int,
        authentication: Authentication,
    ): Flux<ProblemSetMetadata> =
        problemSetService.searchMemberProblemSets(query, problemKey, PageRequest.of(page, size), authentication).flatMap {
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

    // endregion

    // region item

    @GetMapping("/{shareCode}")
    @SecurityRequirements
    @Operation(
        summary = "Get problem set by share code",
        description =
            "Retrieves a problem set by its share code. Returns 404 when the set does not exist or is private " +
                "and the caller is not a member (to avoid leaking existence of private sets).",
    )
    @ApiResponses(
        value =
            [
                ApiResponse(responseCode = "200", description = "Successfully retrieved problem set"),
                ApiResponse(responseCode = "404", description = "Problem set not found or not visible to caller"),
            ]
    )
    fun getProblemSetByShareCode(
        @PathVariable @NotBlank shareCode: String,
        authentication: Authentication?,
    ): Mono<ProblemSetDto> =
        problemSetService
            .findByShareCode(shareCode)
            .filter { problemSetPermissionEvaluator.hasReadPermission(it, authentication?.id()) }
            .orNotFound("ProblemSet [$shareCode] not found")
            .flatMap { problemSetMapper.toDto(it, authentication) }

    @PatchMapping("/{shareCode}")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "cookieAuth")
    @Operation(
        summary = "Update problem set settings",
        description =
            "Partially updates one or more configuration fields (name, description, isPublic, problemKeys). " +
                "Null fields are left unchanged. Requires moderator access.",
    )
    @ApiResponses(
        value =
            [
                ApiResponse(responseCode = "200", description = "Settings updated"),
                ApiResponse(responseCode = "400", description = "Validation failed (e.g. empty body)"),
                ApiResponse(responseCode = "401", description = "Unauthorized"),
                ApiResponse(responseCode = "403", description = "Caller does not have moderator access"),
                ApiResponse(responseCode = "404", description = "Problem set not found"),
            ]
    )
    fun updateSettings(
        @PathVariable @NotBlank shareCode: String,
        @RequestBody @Valid request: UpdateProblemSetSettingsRequest,
        authentication: Authentication,
    ): Mono<ProblemSetDto> =
        problemSetService.updateSettings(shareCode, request, authentication).flatMap {
            problemSetMapper.toDto(it, authentication)
        }

    // endregion

    // region members

    @GetMapping("/{shareCode}/members")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "cookieAuth")
    @Operation(summary = "List members", description = "Lists active members and their roles. Requires moderator access.")
    @ApiResponses(
        value =
            [
                ApiResponse(responseCode = "200", description = "Successfully retrieved members"),
                ApiResponse(responseCode = "401", description = "Unauthorized"),
                ApiResponse(responseCode = "403", description = "Caller does not have moderator access"),
                ApiResponse(responseCode = "404", description = "Problem set not found"),
            ]
    )
    fun getMembers(@PathVariable @NotBlank shareCode: String, authentication: Authentication): Flux<UserNameWithRole> =
        problemSetService.loadForModerator(shareCode, authentication).flatMapMany { problemSetMapper.toUserRoles(it) }

    @PutMapping("/{shareCode}/members/{username}")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "cookieAuth")
    @Operation(
        summary = "Set member role",
        description = "Assigns a role to an existing member. Requires moderator access and a higher role than the target.",
    )
    @ApiResponses(
        value =
            [
                ApiResponse(responseCode = "200", description = "Role updated"),
                ApiResponse(responseCode = "400", description = "Validation failed or would leave no admin"),
                ApiResponse(responseCode = "401", description = "Unauthorized"),
                ApiResponse(responseCode = "403", description = "Caller does not have sufficient permissions"),
                ApiResponse(responseCode = "404", description = "Problem set or user not found"),
            ]
    )
    fun setMemberRole(
        @PathVariable @NotBlank shareCode: String,
        @PathVariable @NotBlank username: String,
        @RequestBody @Valid request: SetMemberRoleRequest,
        authentication: Authentication,
    ): Mono<ProblemSetDto> =
        userService
            .findUserByName(username)
            .orNotFound("User $username not found")
            .flatMap { user ->
                problemSetService.setMemberRole(shareCode, requireNotNull(user.id), request.role, authentication)
            }
            .flatMap { problemSetMapper.toDto(it, authentication) }

    @DeleteMapping("/{shareCode}/members/{username}")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "cookieAuth")
    @Operation(
        summary = "Remove member",
        description = "Removes a user from the problem set. Requires moderator access and a higher role than the target.",
    )
    @ApiResponses(
        value =
            [
                ApiResponse(responseCode = "200", description = "Member removed"),
                ApiResponse(responseCode = "400", description = "Would leave no admin"),
                ApiResponse(responseCode = "401", description = "Unauthorized"),
                ApiResponse(responseCode = "403", description = "Caller does not have sufficient permissions"),
                ApiResponse(responseCode = "404", description = "Problem set or user not found"),
            ]
    )
    fun removeMember(
        @PathVariable @NotBlank shareCode: String,
        @PathVariable @NotBlank username: String,
        authentication: Authentication,
    ): Mono<ProblemSetDto> =
        userService
            .findUserByName(username)
            .orNotFound("User $username not found")
            .flatMap { user -> problemSetService.removeMember(shareCode, requireNotNull(user.id), authentication) }
            .flatMap { problemSetMapper.toDto(it, authentication) }

    @DeleteMapping("/{shareCode}/members/me")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "cookieAuth")
    @Operation(summary = "Leave problem set", description = "Removes the authenticated user from the problem set")
    @ApiResponses(
        value =
            [
                ApiResponse(responseCode = "204", description = "Successfully left problem set"),
                ApiResponse(responseCode = "400", description = "Caller is not a member or is the last admin"),
                ApiResponse(responseCode = "401", description = "Unauthorized"),
                ApiResponse(responseCode = "404", description = "Problem set not found"),
            ]
    )
    fun leaveProblemSet(@PathVariable @NotBlank shareCode: String, authentication: Authentication): Mono<Void> =
        problemSetService.leaveProblemSet(shareCode, authentication).then()

    // endregion

    // region invitations

    @GetMapping("/{shareCode}/invitations")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "cookieAuth")
    @Operation(summary = "List pending invitations", description = "Lists pending invitees. Requires moderator access.")
    @ApiResponses(
        value =
            [
                ApiResponse(responseCode = "200", description = "Successfully retrieved invitations"),
                ApiResponse(responseCode = "401", description = "Unauthorized"),
                ApiResponse(responseCode = "403", description = "Caller does not have moderator access"),
                ApiResponse(responseCode = "404", description = "Problem set not found"),
            ]
    )
    fun getInvitations(@PathVariable @NotBlank shareCode: String, authentication: Authentication): Mono<List<String>> =
        problemSetService
            .loadForModerator(shareCode, authentication)
            .flatMapMany { problemSetMapper.toInvitedUserNames(it) }
            .collectList()

    @PostMapping("/{shareCode}/invitations")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "cookieAuth")
    @Operation(
        summary = "Invite user",
        description = "Creates a pending invitation and notifies the invitee. Requires moderator access.",
    )
    @ApiResponses(
        value =
            [
                ApiResponse(responseCode = "200", description = "Invitation created"),
                ApiResponse(responseCode = "400", description = "Validation failed or user is already a member/invitee"),
                ApiResponse(responseCode = "401", description = "Unauthorized"),
                ApiResponse(responseCode = "403", description = "Caller does not have moderator access"),
                ApiResponse(responseCode = "404", description = "Problem set or invitee not found"),
            ]
    )
    fun createInvitation(
        @PathVariable @NotBlank shareCode: String,
        @RequestBody @Valid request: CreateInvitationRequest,
        authentication: Authentication,
    ): Mono<ProblemSetDto> =
        userService
            .findUserByName(request.inviteeName)
            .orNotFound("User ${request.inviteeName} not found")
            .flatMap { invitee -> problemSetService.createInvitation(shareCode, requireNotNull(invitee.id), authentication) }
            .flatMap { problemSetMapper.toDto(it, authentication) }

    @DeleteMapping("/{shareCode}/invitations/{username}")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "cookieAuth")
    @Operation(summary = "Revoke invitation", description = "Cancels a pending invitation. Requires moderator access.")
    @ApiResponses(
        value =
            [
                ApiResponse(responseCode = "200", description = "Invitation revoked"),
                ApiResponse(responseCode = "400", description = "User has no pending invitation"),
                ApiResponse(responseCode = "401", description = "Unauthorized"),
                ApiResponse(responseCode = "403", description = "Caller does not have moderator access"),
                ApiResponse(responseCode = "404", description = "Problem set or invitee not found"),
            ]
    )
    fun revokeInvitation(
        @PathVariable @NotBlank shareCode: String,
        @PathVariable @NotBlank username: String,
        authentication: Authentication,
    ): Mono<ProblemSetDto> =
        userService
            .findUserByName(username)
            .orNotFound("User $username not found")
            .flatMap { invitee -> problemSetService.revokeInvitation(shareCode, requireNotNull(invitee.id), authentication) }
            .flatMap { problemSetMapper.toDto(it, authentication) }

    @PostMapping("/{shareCode}/invitations/me/accept")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "cookieAuth")
    @Operation(summary = "Accept invitation", description = "Accepts a pending invitation addressed to the caller")
    @ApiResponses(
        value =
            [
                ApiResponse(responseCode = "200", description = "Invitation accepted"),
                ApiResponse(responseCode = "401", description = "Unauthorized"),
                ApiResponse(responseCode = "403", description = "No pending invitation for the caller"),
                ApiResponse(responseCode = "404", description = "Problem set not found"),
            ]
    )
    fun acceptInvitation(@PathVariable @NotBlank shareCode: String, authentication: Authentication): Mono<ProblemSetDto> =
        problemSetService.acceptInvitation(shareCode, authentication).flatMap { problemSetMapper.toDto(it, authentication) }

    @PostMapping("/{shareCode}/invitations/me/decline")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "cookieAuth")
    @Operation(summary = "Decline invitation", description = "Declines a pending invitation addressed to the caller")
    @ApiResponses(
        value =
            [
                ApiResponse(responseCode = "204", description = "Invitation declined"),
                ApiResponse(responseCode = "401", description = "Unauthorized"),
                ApiResponse(responseCode = "403", description = "No pending invitation for the caller"),
                ApiResponse(responseCode = "404", description = "Problem set not found"),
            ]
    )
    fun declineInvitation(@PathVariable @NotBlank shareCode: String, authentication: Authentication): Mono<Void> =
        problemSetService.declineInvitation(shareCode, authentication).then()

    // endregion
}
