package io.github.sanyavertolet.edukate.backend.controllers

import io.github.sanyavertolet.edukate.backend.dtos.BundleDto
import io.github.sanyavertolet.edukate.backend.dtos.BundleMetadata
import io.github.sanyavertolet.edukate.backend.dtos.ChangeBundleProblemsRequest
import io.github.sanyavertolet.edukate.backend.dtos.CreateBundleRequest
import io.github.sanyavertolet.edukate.backend.dtos.UserNameWithRole
import io.github.sanyavertolet.edukate.backend.entities.Bundle
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
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
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

@RestController
@Validated
@RequestMapping("/api/v1/bundles")
@Tag(name = "Bundles", description = "API for managing problem bundles and user collaborations")
@Suppress("TooManyFunctions")
class BundleController(
    private val bundleService: BundleService,
    private val userService: UserService,
    private val notifier: Notifier,
) {
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create a bundle", description = "Creates a new bundle with the specified properties")
    @ApiResponses(
        value =
            [
                ApiResponse(
                    responseCode = "200",
                    description = "Successfully created bundle",
                    content = [Content(schema = Schema(implementation = BundleDto::class))],
                ),
                ApiResponse(responseCode = "400", description = "Validation failed", content = [Content()]),
                ApiResponse(responseCode = "401", description = "Unauthorized", content = [Content()]),
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
    @Operation(
        summary = "Get owned bundles",
        description = "Retrieves a paginated list of bundles owned by the authenticated user",
    )
    @ApiResponses(
        value =
            [
                ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved owned bundles",
                    content = [Content(array = ArraySchema(schema = Schema(implementation = BundleMetadata::class)))],
                ),
                ApiResponse(responseCode = "400", description = "Validation failed", content = [Content()]),
                ApiResponse(responseCode = "401", description = "Unauthorized", content = [Content()]),
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
    @Operation(
        summary = "Get joined bundles",
        description = "Retrieves a paginated list of bundles joined by the authenticated user",
    )
    @ApiResponses(
        value =
            [
                ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved joined bundles",
                    content = [Content(array = ArraySchema(schema = Schema(implementation = BundleMetadata::class)))],
                ),
                ApiResponse(responseCode = "400", description = "Validation failed", content = [Content()]),
                ApiResponse(responseCode = "401", description = "Unauthorized", content = [Content()]),
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
                ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved public bundles",
                    content = [Content(array = ArraySchema(schema = Schema(implementation = BundleMetadata::class)))],
                ),
                ApiResponse(responseCode = "400", description = "Validation failed", content = [Content()]),
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
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "Get bundle by share code",
        description = "Retrieves a bundle by its share code (user must be a member of the bundle)",
    )
    @ApiResponses(
        value =
            [
                ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved bundle",
                    content = [Content(schema = Schema(implementation = BundleDto::class))],
                ),
                ApiResponse(responseCode = "400", description = "Validation failed", content = [Content()]),
                ApiResponse(responseCode = "401", description = "Unauthorized", content = [Content()]),
                ApiResponse(
                    responseCode = "403",
                    description = "Access denied - User is not a member of the bundle",
                    content = [Content()],
                ),
                ApiResponse(responseCode = "404", description = "Bundle not found", content = [Content()]),
            ]
    )
    @Parameters(
        value = [Parameter(name = "shareCode", description = "Bundle share code", `in` = ParameterIn.PATH, required = true)]
    )
    fun getBundleByShareCode(@PathVariable @NotBlank shareCode: String, authentication: Authentication): Mono<BundleDto> =
        bundleService
            .findBundleByShareCode(shareCode)
            .filter { it.isUserInBundle(authentication.id()) }
            .switchIfEmpty(
                Mono.error(ResponseStatusException(HttpStatus.FORBIDDEN, "You must be a member of the bundle to view it."))
            )
            .flatMap { bundle -> bundleService.prepareDto(bundle, authentication) }

    @PostMapping("/{shareCode}/join")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Join a bundle", description = "Joins a bundle using its share code")
    @ApiResponses(
        value =
            [
                ApiResponse(
                    responseCode = "200",
                    description = "Successfully joined bundle",
                    content = [Content(schema = Schema(implementation = BundleMetadata::class))],
                ),
                ApiResponse(responseCode = "400", description = "Already in bundle", content = [Content()]),
                ApiResponse(responseCode = "401", description = "Unauthorized", content = [Content()]),
                ApiResponse(
                    responseCode = "403",
                    description = "Access denied - invite is required to join",
                    content = [Content()],
                ),
                ApiResponse(responseCode = "404", description = "Bundle not found", content = [Content()]),
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
    @Operation(
        summary = "Leave a bundle",
        description = "Leaves a bundle the user is currently a member of and returns bundle shareCode",
    )
    @ApiResponses(
        value =
            [
                ApiResponse(
                    responseCode = "200",
                    description = "Successfully left bundle",
                    content = [Content(schema = Schema(implementation = String::class))],
                ),
                ApiResponse(
                    responseCode = "400",
                    description = "Either not a participant or last admin",
                    content = [Content()],
                ),
                ApiResponse(responseCode = "401", description = "Unauthorized", content = [Content()]),
                ApiResponse(responseCode = "404", description = "Bundle not found", content = [Content()]),
            ]
    )
    @Parameters(
        value = [Parameter(name = "shareCode", description = "Bundle share code", `in` = ParameterIn.PATH, required = true)]
    )
    fun leaveBundle(@PathVariable @NotBlank shareCode: String, authentication: Authentication): Mono<String> =
        authentication.monoId().flatMap { userId -> bundleService.removeUser(shareCode, userId) }.map { it.shareCode }

    @PostMapping("/{shareCode}/invite")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Invite user to bundle", description = "Invites another user to join a bundle")
    @ApiResponses(
        value =
            [
                ApiResponse(
                    responseCode = "200",
                    description = "Successfully invited user",
                    content = [Content(schema = Schema(implementation = String::class))],
                ),
                ApiResponse(responseCode = "400", description = "User is already in bundle", content = [Content()]),
                ApiResponse(responseCode = "401", description = "Unauthorized", content = [Content()]),
                ApiResponse(
                    responseCode = "403",
                    description = "Access denied - Insufficient permissions in bundle",
                    content = [Content()],
                ),
                ApiResponse(responseCode = "404", description = "Bundle or user not found", content = [Content()]),
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
            .switchIfEmpty(Mono.error(ResponseStatusException(HttpStatus.NOT_FOUND, "User $inviteeName not found")))
            .zipWhen(
                { invitee ->
                    val requesterId = requireNotNull(authentication.id())
                    bundleService.inviteUser(shareCode, requesterId, invitee.id!!)
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
    @Operation(summary = "Expire bundle invitation", description = "Cancels an existing invitation for a specific user")
    @ApiResponses(
        value =
            [
                ApiResponse(
                    responseCode = "200",
                    description = "Successfully expired invitation",
                    content = [Content(schema = Schema(implementation = String::class))],
                ),
                ApiResponse(responseCode = "400", description = "User is not invited", content = [Content()]),
                ApiResponse(responseCode = "401", description = "Unauthorized", content = [Content()]),
                ApiResponse(
                    responseCode = "403",
                    description = "Access denied - Insufficient permissions in bundle",
                    content = [Content()],
                ),
                ApiResponse(responseCode = "404", description = "Bundle or user not found", content = [Content()]),
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
            .switchIfEmpty(Mono.error(ResponseStatusException(HttpStatus.NOT_FOUND, "User $inviteeName not found")))
            .flatMap { invitee ->
                val requesterId = requireNotNull(authentication.id())
                bundleService.expireInvite(shareCode, requesterId, invitee.id!!)
            }
            .thenReturn("Invitation for user $inviteeName has been expired in bundle $shareCode")

    @PostMapping("/{shareCode}/reply-invite")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Reply to bundle invitation", description = "Accepts or declines an invitation to join a bundle")
    @ApiResponses(
        value =
            [
                ApiResponse(
                    responseCode = "200",
                    description = "Successfully replied to invitation",
                    content = [Content(schema = Schema(implementation = String::class))],
                ),
                ApiResponse(responseCode = "400", description = "User is already in bundle", content = [Content()]),
                ApiResponse(responseCode = "401", description = "Unauthorized", content = [Content()]),
                ApiResponse(
                    responseCode = "403",
                    description = "Access denied - User is not invited",
                    content = [Content()],
                ),
                ApiResponse(responseCode = "404", description = "Bundle not found", content = [Content()]),
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
        Mono.just(response).flatMap { hasAccepted ->
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
    @Operation(summary = "Get users in bundle", description = "Retrieves a list of users in a bundle with their roles")
    @ApiResponses(
        value =
            [
                ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved users",
                    content = [Content(array = ArraySchema(schema = Schema(implementation = UserNameWithRole::class)))],
                ),
                ApiResponse(responseCode = "400", description = "Validation failed", content = [Content()]),
                ApiResponse(responseCode = "401", description = "Unauthorized", content = [Content()]),
                ApiResponse(
                    responseCode = "403",
                    description = "Access denied - Insufficient bundle permissions",
                    content = [Content()],
                ),
                ApiResponse(responseCode = "404", description = "Bundle not found", content = [Content()]),
            ]
    )
    fun getUserRoles(@PathVariable @NotBlank shareCode: String, authentication: Authentication): Flux<UserNameWithRole> =
        bundleService.getBundleUsers(shareCode, authentication)

    @GetMapping("/{shareCode}/invited-users")
    @Operation(
        summary = "Get invited user names in bundle",
        description = "Retrieves the list of invited (pending) user names",
    )
    @ApiResponses(
        value =
            [
                ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved invited user names",
                    content = [Content(array = ArraySchema(schema = Schema(implementation = String::class)))],
                ),
                ApiResponse(responseCode = "400", description = "Validation failed", content = [Content()]),
                ApiResponse(responseCode = "401", description = "Unauthorized", content = [Content()]),
                ApiResponse(
                    responseCode = "403",
                    description = "Access denied - Insufficient bundle permissions",
                    content = [Content()],
                ),
                ApiResponse(responseCode = "404", description = "Bundle not found", content = [Content()]),
            ]
    )
    fun getInvitedUsers(@PathVariable @NotBlank shareCode: String, authentication: Authentication): Mono<List<String>> =
        bundleService.getBundleInvitedUsers(shareCode, authentication).collectList()

    @PostMapping("/{shareCode}/role")
    @Operation(summary = "Change user role in bundle", description = "Changes user role in a bundle by their name")
    @ApiResponses(
        value =
            [
                ApiResponse(
                    responseCode = "200",
                    description = "Successfully changed user role",
                    content = [Content(schema = Schema(implementation = UserRole::class))],
                ),
                ApiResponse(responseCode = "401", description = "Unauthorized", content = [Content()]),
                ApiResponse(
                    responseCode = "403",
                    description = "Access denied - Insufficient bundle permissions",
                    content = [Content()],
                ),
                ApiResponse(responseCode = "404", description = "Bundle or user not found", content = [Content()]),
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
            .switchIfEmpty(Mono.error(ResponseStatusException(HttpStatus.NOT_FOUND, "User $username not found")))
            .mapNotNull { it.id }
            .flatMap { userId -> bundleService.changeUserRole(shareCode, userId, requestedRole, authentication) }

    @PostMapping("/{shareCode}/visibility")
    @Operation(
        summary = "Change bundle visibility",
        description = "Changes bundle visibility allowing it to be public or private",
    )
    @ApiResponses(
        value =
            [
                ApiResponse(
                    responseCode = "200",
                    description = "Successfully changed visibility",
                    content = [Content(schema = Schema(implementation = BundleDto::class))],
                ),
                ApiResponse(
                    responseCode = "403",
                    description = "Access denied - Insufficient bundle permissions",
                    content = [Content()],
                ),
                ApiResponse(responseCode = "404", description = "Bundle not found", content = [Content()]),
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
    @Operation(summary = "Change bundle problems", description = "Changes problem list of a bundle")
    @ApiResponses(
        value =
            [
                ApiResponse(
                    responseCode = "200",
                    description = "Successfully changed problem list",
                    content = [Content(schema = Schema(implementation = BundleDto::class))],
                ),
                ApiResponse(responseCode = "400", description = "Problem list should not be empty", content = [Content()]),
                ApiResponse(
                    responseCode = "403",
                    description = "Access denied - Insufficient bundle permissions",
                    content = [Content()],
                ),
                ApiResponse(responseCode = "404", description = "Bundle not found", content = [Content()]),
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
