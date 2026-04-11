package io.github.sanyavertolet.edukate.backend.controllers

import io.github.sanyavertolet.edukate.backend.dtos.BundleDto
import io.github.sanyavertolet.edukate.backend.dtos.BundleMetadata
import io.github.sanyavertolet.edukate.backend.dtos.ChangeBundleProblemsRequest
import io.github.sanyavertolet.edukate.backend.dtos.CreateBundleRequest
import io.github.sanyavertolet.edukate.backend.dtos.UserNameWithRole
import io.github.sanyavertolet.edukate.backend.entities.Bundle
import io.github.sanyavertolet.edukate.backend.permissions.BundlePermissionEvaluator
import io.github.sanyavertolet.edukate.backend.services.BundleService
import io.github.sanyavertolet.edukate.backend.services.UserService
import io.github.sanyavertolet.edukate.common.notifications.InviteNotificationCreateRequest
import io.github.sanyavertolet.edukate.common.services.Notifier
import io.github.sanyavertolet.edukate.common.users.UserRole
import io.github.sanyavertolet.edukate.common.utils.id
import io.github.sanyavertolet.edukate.common.utils.monoId
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.Parameters
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.PositiveOrZero
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
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
import org.springframework.web.server.ResponseStatusException
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

@RestController
@Validated
@RequestMapping("/api/v1/bundles")
@Tag(name = "Bundles", description = "API for managing problem bundles and user collaborations")
@Suppress("TooManyFunctions")
class BundleController(
    private val bundleService: BundleService,
    private val userService: UserService,
    private val notifier: Notifier,
    private val bundlePermissionEvaluator: BundlePermissionEvaluator,
) {
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "cookieAuth")
    @Operation(summary = "Create a bundle", description = "Creates a new bundle with the specified properties")
    @ApiResponses(
        value =
            [
                ApiResponse(responseCode = "200", description = "Successfully created bundle"),
                ApiResponse(responseCode = "400", description = "Validation failed"),
                ApiResponse(responseCode = "401", description = "Unauthorized"),
            ]
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
        required = true,
        content =
            [
                Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = Schema(implementation = CreateBundleRequest::class),
                )
            ],
    )
    fun createBundle(@RequestBody @Valid request: CreateBundleRequest, authentication: Authentication): Mono<BundleDto> =
        bundleService.createBundle(request, authentication).flatMap { bundle ->
            bundleService.prepareDto(bundle, authentication)
        }

    @GetMapping("/owned")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "cookieAuth")
    @Operation(
        summary = "Get owned bundles",
        description = "Retrieves a paginated list of bundles owned by the authenticated user",
    )
    @ApiResponses(
        value =
            [
                ApiResponse(responseCode = "200", description = "Successfully retrieved owned bundles"),
                ApiResponse(responseCode = "400", description = "Validation failed"),
                ApiResponse(responseCode = "401", description = "Unauthorized"),
            ]
    )
    @Parameters(
        value =
            [
                Parameter(
                    name = "page",
                    description = "Page number (zero-based)",
                    `in` = ParameterIn.QUERY,
                    schema = Schema(minimum = "0"),
                ),
                Parameter(
                    name = "size",
                    description = "Number of bundles per page",
                    `in` = ParameterIn.QUERY,
                    schema = Schema(minimum = "1", maximum = "100"),
                ),
            ]
    )
    fun getOwnedBundles(
        @RequestParam(defaultValue = "0") @PositiveOrZero page: Int,
        @RequestParam(defaultValue = "10") @Positive size: Int,
        authentication: Authentication,
    ): Flux<BundleMetadata> =
        bundleService.getOwnedBundles(PageRequest.of(page, size), authentication).flatMap {
            bundleService.prepareMetadata(it)
        }

    @GetMapping("/joined")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "cookieAuth")
    @Operation(
        summary = "Get joined bundles",
        description = "Retrieves a paginated list of bundles joined by the authenticated user",
    )
    @ApiResponses(
        value =
            [
                ApiResponse(responseCode = "200", description = "Successfully retrieved joined bundles"),
                ApiResponse(responseCode = "400", description = "Validation failed"),
                ApiResponse(responseCode = "401", description = "Unauthorized"),
            ]
    )
    @Parameters(
        value =
            [
                Parameter(
                    name = "page",
                    description = "Page number (zero-based)",
                    `in` = ParameterIn.QUERY,
                    schema = Schema(minimum = "0"),
                ),
                Parameter(
                    name = "size",
                    description = "Number of bundles per page",
                    `in` = ParameterIn.QUERY,
                    schema = Schema(minimum = "1", maximum = "100"),
                ),
            ]
    )
    fun getJoinedBundles(
        @RequestParam(defaultValue = "0") @PositiveOrZero page: Int,
        @RequestParam(defaultValue = "10") @Positive size: Int,
        authentication: Authentication,
    ): Flux<BundleMetadata> =
        bundleService.getJoinedBundles(PageRequest.of(page, size), authentication).flatMap {
            bundleService.prepareMetadata(it)
        }

    @GetMapping("/public")
    @Operation(summary = "Get public bundles", description = "Retrieves a paginated list of public bundles")
    @ApiResponses(
        value =
            [
                ApiResponse(responseCode = "200", description = "Successfully retrieved public bundles"),
                ApiResponse(responseCode = "400", description = "Validation failed"),
            ]
    )
    @Parameters(
        value =
            [
                Parameter(
                    name = "page",
                    description = "Page number (zero-based)",
                    `in` = ParameterIn.QUERY,
                    schema = Schema(minimum = "0"),
                ),
                Parameter(
                    name = "size",
                    description = "Number of bundles per page",
                    `in` = ParameterIn.QUERY,
                    schema = Schema(minimum = "1", maximum = "100"),
                ),
            ]
    )
    fun getPublicBundles(
        @RequestParam(defaultValue = "0") @PositiveOrZero page: Int,
        @RequestParam(defaultValue = "10") @Positive size: Int,
    ): Flux<BundleMetadata> =
        bundleService.getPublicBundles(PageRequest.of(page, size)).flatMap { bundleService.prepareMetadata(it) }

    @GetMapping("/{shareCode}")
    @Operation(
        summary = "Get bundle by share code",
        description =
            "Retrieves a bundle by its share code. " +
                "Public bundles are accessible to anyone; private bundles require membership.",
    )
    @ApiResponses(
        value =
            [
                ApiResponse(responseCode = "200", description = "Successfully retrieved bundle"),
                ApiResponse(responseCode = "400", description = "Validation failed"),
                ApiResponse(
                    responseCode = "403",
                    description = "Access denied - bundle is private and user is not a member",
                ),
                ApiResponse(responseCode = "404", description = "Bundle not found"),
            ]
    )
    @Parameters(
        value = [Parameter(name = "shareCode", description = "Bundle share code", `in` = ParameterIn.PATH, required = true)]
    )
    fun getBundleByShareCode(@PathVariable @NotBlank shareCode: String, authentication: Authentication?): Mono<BundleDto> =
        bundleService
            .findBundleByShareCode(shareCode)
            .filter { bundlePermissionEvaluator.hasReadPermission(it, authentication?.id()) }
            .switchIfEmpty(
                ResponseStatusException(HttpStatus.FORBIDDEN, "Bundle is private and you are not a member.").toMono()
            )
            .flatMap { bundle -> bundleService.prepareDto(bundle, authentication) }

    @PostMapping("/{shareCode}/join")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "cookieAuth")
    @Operation(summary = "Join a bundle", description = "Joins a bundle using its share code")
    @ApiResponses(
        value =
            [
                ApiResponse(responseCode = "200", description = "Successfully joined bundle"),
                ApiResponse(responseCode = "400", description = "Already in bundle"),
                ApiResponse(responseCode = "401", description = "Unauthorized"),
                ApiResponse(responseCode = "403", description = "Access denied - invite is required to join"),
                ApiResponse(responseCode = "404", description = "Bundle not found"),
            ]
    )
    @Parameters(
        value = [Parameter(name = "shareCode", description = "Bundle share code", `in` = ParameterIn.PATH, required = true)]
    )
    fun joinBundle(@PathVariable @NotBlank shareCode: String, authentication: Authentication): Mono<BundleMetadata> =
        authentication
            .monoId()
            .flatMap { userId -> bundleService.joinUser(shareCode, userId) }
            .flatMap { bundleService.prepareMetadata(it) }

    @PostMapping("/{shareCode}/leave")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "cookieAuth")
    @Operation(
        summary = "Leave a bundle",
        description = "Leaves a bundle the user is currently a member of and returns bundle shareCode",
    )
    @ApiResponses(
        value =
            [
                ApiResponse(responseCode = "200", description = "Successfully left bundle"),
                ApiResponse(responseCode = "400", description = "Either not a participant or last admin"),
                ApiResponse(responseCode = "401", description = "Unauthorized"),
                ApiResponse(responseCode = "404", description = "Bundle not found"),
            ]
    )
    @Parameters(
        value = [Parameter(name = "shareCode", description = "Bundle share code", `in` = ParameterIn.PATH, required = true)]
    )
    fun leaveBundle(@PathVariable @NotBlank shareCode: String, authentication: Authentication): Mono<String> =
        authentication.monoId().flatMap { userId -> bundleService.removeUser(shareCode, userId) }.map { it.shareCode }

    @PostMapping("/{shareCode}/invite")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "cookieAuth")
    @Operation(summary = "Invite user to bundle", description = "Invites another user to join a bundle")
    @ApiResponses(
        value =
            [
                ApiResponse(responseCode = "200", description = "Successfully invited user"),
                ApiResponse(responseCode = "400", description = "User is already in bundle"),
                ApiResponse(responseCode = "401", description = "Unauthorized"),
                ApiResponse(responseCode = "403", description = "Access denied - Insufficient permissions in bundle"),
                ApiResponse(responseCode = "404", description = "Bundle or user not found"),
            ]
    )
    @Parameters(
        value =
            [
                Parameter(name = "shareCode", description = "Bundle share code", `in` = ParameterIn.PATH, required = true),
                Parameter(
                    name = "inviteeName",
                    description = "Username of the user to invite",
                    `in` = ParameterIn.QUERY,
                    required = true,
                ),
            ]
    )
    fun inviteToBundle(
        @PathVariable @NotBlank shareCode: String,
        @RequestParam @NotBlank inviteeName: String,
        authentication: Authentication,
    ): Mono<String> =
        userService
            .findUserByName(inviteeName)
            .switchIfEmpty(ResponseStatusException(HttpStatus.NOT_FOUND, "User $inviteeName not found").toMono())
            .zipWhen(
                { invitee ->
                    val requesterId = requireNotNull(authentication.id())
                    bundleService.inviteUser(shareCode, requesterId, requireNotNull(invitee.id))
                },
                { invitee, bundle -> prepareNotification(invitee, bundle, authentication.name) },
            )
            .flatMap { notifier.notify(it) }
            .thenReturn("User $inviteeName has been invited to bundle $shareCode")

    private fun prepareNotification(
        user: io.github.sanyavertolet.edukate.backend.entities.User,
        bundle: Bundle,
        inviterName: String,
    ): InviteNotificationCreateRequest {
        val userId = requireNotNull(user.id) { "User ID must not be null" }
        return InviteNotificationCreateRequest.from(userId, inviterName, bundle.name, bundle.shareCode)
    }

    @PostMapping("/{shareCode}/expire-invite")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "cookieAuth")
    @Operation(summary = "Expire bundle invitation", description = "Cancels an existing invitation for a specific user")
    @ApiResponses(
        value =
            [
                ApiResponse(responseCode = "200", description = "Successfully expired invitation"),
                ApiResponse(responseCode = "400", description = "User is not invited"),
                ApiResponse(responseCode = "401", description = "Unauthorized"),
                ApiResponse(responseCode = "403", description = "Access denied - Insufficient permissions in bundle"),
                ApiResponse(responseCode = "404", description = "Bundle or user not found"),
            ]
    )
    @Parameters(
        value =
            [
                Parameter(name = "shareCode", description = "Bundle share code", `in` = ParameterIn.PATH, required = true),
                Parameter(
                    name = "inviteeName",
                    description = "Username of the user whose invitation to cancel",
                    `in` = ParameterIn.QUERY,
                    required = true,
                ),
            ]
    )
    fun expireInvite(
        @PathVariable @NotBlank shareCode: String,
        @RequestParam @NotBlank inviteeName: String,
        authentication: Authentication,
    ): Mono<String> =
        userService
            .findUserByName(inviteeName)
            .switchIfEmpty(ResponseStatusException(HttpStatus.NOT_FOUND, "User $inviteeName not found").toMono())
            .flatMap { invitee ->
                val requesterId = requireNotNull(authentication.id())
                bundleService.expireInvite(shareCode, requesterId, requireNotNull(invitee.id))
            }
            .thenReturn("Invitation for user $inviteeName has been expired in bundle $shareCode")

    @PostMapping("/{shareCode}/reply-invite")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "cookieAuth")
    @Operation(summary = "Reply to bundle invitation", description = "Accepts or declines an invitation to join a bundle")
    @ApiResponses(
        value =
            [
                ApiResponse(responseCode = "200", description = "Successfully replied to invitation"),
                ApiResponse(responseCode = "400", description = "User is already in bundle"),
                ApiResponse(responseCode = "401", description = "Unauthorized"),
                ApiResponse(responseCode = "403", description = "Access denied - User is not invited"),
                ApiResponse(responseCode = "404", description = "Bundle not found"),
            ]
    )
    @Parameters(
        value =
            [
                Parameter(name = "shareCode", description = "Bundle share code", `in` = ParameterIn.PATH, required = true),
                Parameter(
                    name = "response",
                    description = "True to accept, false to decline",
                    `in` = ParameterIn.QUERY,
                    required = true,
                ),
            ]
    )
    fun replyToInvite(
        @PathVariable @NotBlank shareCode: String,
        @RequestParam @NotNull response: Boolean,
        authentication: Authentication,
    ): Mono<String> =
        response.toMono().flatMap { hasAccepted ->
            if (hasAccepted) {
                bundleService
                    .joinUser(shareCode, requireNotNull(authentication.id()))
                    .thenReturn("You have accepted invite to bundle $shareCode")
            } else {
                bundleService
                    .declineInvite(shareCode, authentication)
                    .thenReturn("You have declined invite to bundle $shareCode")
            }
        }

    @GetMapping("/{shareCode}/users")
    @SecurityRequirement(name = "cookieAuth")
    @Operation(summary = "Get users in bundle", description = "Retrieves a list of users in a bundle with their roles")
    @ApiResponses(
        value =
            [
                ApiResponse(responseCode = "200", description = "Successfully retrieved users"),
                ApiResponse(responseCode = "400", description = "Validation failed"),
                ApiResponse(responseCode = "401", description = "Unauthorized"),
                ApiResponse(responseCode = "403", description = "Access denied - Insufficient bundle permissions"),
                ApiResponse(responseCode = "404", description = "Bundle not found"),
            ]
    )
    @Parameters(
        value = [Parameter(name = "shareCode", description = "Bundle share code", `in` = ParameterIn.PATH, required = true)]
    )
    fun getUserRoles(@PathVariable @NotBlank shareCode: String, authentication: Authentication): Flux<UserNameWithRole> =
        bundleService.getBundleUsers(shareCode, authentication)

    @GetMapping("/{shareCode}/invited-users")
    @SecurityRequirement(name = "cookieAuth")
    @Operation(
        summary = "Get invited user names in bundle",
        description = "Retrieves the list of invited (pending) user names",
    )
    @ApiResponses(
        value =
            [
                ApiResponse(responseCode = "200", description = "Successfully retrieved invited user names"),
                ApiResponse(responseCode = "400", description = "Validation failed"),
                ApiResponse(responseCode = "401", description = "Unauthorized"),
                ApiResponse(responseCode = "403", description = "Access denied - Insufficient bundle permissions"),
                ApiResponse(responseCode = "404", description = "Bundle not found"),
            ]
    )
    @Parameters(
        value = [Parameter(name = "shareCode", description = "Bundle share code", `in` = ParameterIn.PATH, required = true)]
    )
    fun getInvitedUsers(@PathVariable @NotBlank shareCode: String, authentication: Authentication): Mono<List<String>> =
        bundleService.getBundleInvitedUsers(shareCode, authentication).collectList()

    @PostMapping("/{shareCode}/role")
    @SecurityRequirement(name = "cookieAuth")
    @Operation(summary = "Change user role in bundle", description = "Changes user role in a bundle by their name")
    @ApiResponses(
        value =
            [
                ApiResponse(responseCode = "200", description = "Successfully changed user role"),
                ApiResponse(responseCode = "401", description = "Unauthorized"),
                ApiResponse(responseCode = "403", description = "Access denied - Insufficient bundle permissions"),
                ApiResponse(responseCode = "404", description = "Bundle or user not found"),
            ]
    )
    @Parameters(
        value =
            [
                Parameter(name = "shareCode", description = "Bundle share code", `in` = ParameterIn.PATH, required = true),
                Parameter(name = "username", description = "Username", `in` = ParameterIn.QUERY, required = true),
                Parameter(name = "requestedRole", description = "Role to set", `in` = ParameterIn.QUERY, required = true),
            ]
    )
    fun changeUserRole(
        @PathVariable @NotBlank shareCode: String,
        @RequestParam @NotBlank username: String,
        @RequestParam @NotNull requestedRole: UserRole,
        authentication: Authentication,
    ): Mono<UserRole> =
        userService
            .findUserByName(username)
            .switchIfEmpty(ResponseStatusException(HttpStatus.NOT_FOUND, "User $username not found").toMono())
            .mapNotNull { it.id }
            .flatMap { userId -> bundleService.changeUserRole(shareCode, userId, requestedRole, authentication) }

    @PostMapping("/{shareCode}/visibility")
    @SecurityRequirement(name = "cookieAuth")
    @Operation(
        summary = "Change bundle visibility",
        description = "Changes bundle visibility allowing it to be public or private",
    )
    @ApiResponses(
        value =
            [
                ApiResponse(responseCode = "200", description = "Successfully changed visibility"),
                ApiResponse(responseCode = "403", description = "Access denied - Insufficient bundle permissions"),
                ApiResponse(responseCode = "404", description = "Bundle not found"),
            ]
    )
    @Parameters(
        value =
            [
                Parameter(name = "shareCode", description = "Bundle share code", `in` = ParameterIn.PATH, required = true),
                Parameter(name = "isPublic", description = "Visibility flag", `in` = ParameterIn.QUERY, required = true),
            ]
    )
    fun changeVisibility(
        @PathVariable @NotBlank shareCode: String,
        @RequestParam @NotNull isPublic: Boolean,
        authentication: Authentication,
    ): Mono<BundleDto> =
        bundleService.changeVisibility(shareCode, isPublic, authentication).flatMap { bundle ->
            bundleService.prepareDto(bundle, authentication)
        }

    @PostMapping("/{shareCode}/problems")
    @SecurityRequirement(name = "cookieAuth")
    @Operation(summary = "Change bundle problems", description = "Changes problem list of a bundle")
    @ApiResponses(
        value =
            [
                ApiResponse(responseCode = "200", description = "Successfully changed problem list"),
                ApiResponse(responseCode = "400", description = "Problem list should not be empty"),
                ApiResponse(responseCode = "403", description = "Access denied - Insufficient bundle permissions"),
                ApiResponse(responseCode = "404", description = "Bundle not found"),
            ]
    )
    @Parameters(
        value = [Parameter(name = "shareCode", description = "Bundle share code", `in` = ParameterIn.PATH, required = true)]
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
        required = true,
        content =
            [
                Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = Schema(implementation = ChangeBundleProblemsRequest::class),
                )
            ],
    )
    fun changeProblems(
        @PathVariable @NotBlank shareCode: String,
        @RequestBody @Valid changeBundleProblemsRequest: ChangeBundleProblemsRequest,
        authentication: Authentication,
    ): Mono<BundleDto> =
        bundleService.changeProblems(shareCode, changeBundleProblemsRequest.problemIds, authentication).flatMap { bundle ->
            bundleService.prepareDto(bundle, authentication)
        }
}
