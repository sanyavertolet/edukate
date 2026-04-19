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
    fun createProblemSet(
        @RequestBody @Valid request: CreateProblemSetRequest,
        authentication: Authentication,
    ): Mono<ProblemSetDto> =
        problemSetService.createProblemSet(request, authentication).flatMap { problemSetMapper.toDto(it, authentication) }

    @GetMapping("/owned")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "cookieAuth")
    fun getOwnedProblemSets(
        @RequestParam(defaultValue = "0") @PositiveOrZero page: Int,
        @RequestParam(defaultValue = "10") @Positive size: Int,
        authentication: Authentication,
    ): Flux<ProblemSetMetadata> =
        problemSetService.getOwnedProblemSets(PageRequest.of(page, size), authentication).flatMap {
            problemSetMapper.toMetadata(it)
        }

    @GetMapping("/joined")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "cookieAuth")
    fun getJoinedProblemSets(
        @RequestParam(defaultValue = "0") @PositiveOrZero page: Int,
        @RequestParam(defaultValue = "10") @Positive size: Int,
        authentication: Authentication,
    ): Flux<ProblemSetMetadata> =
        problemSetService.getJoinedProblemSets(PageRequest.of(page, size), authentication).flatMap {
            problemSetMapper.toMetadata(it)
        }

    @GetMapping("/public")
    @SecurityRequirements
    fun getPublicProblemSets(
        @RequestParam(defaultValue = "0") @PositiveOrZero page: Int,
        @RequestParam(defaultValue = "10") @Positive size: Int,
    ): Flux<ProblemSetMetadata> =
        problemSetService.getPublicProblemSets(PageRequest.of(page, size)).flatMap { problemSetMapper.toMetadata(it) }

    @GetMapping("/{shareCode}")
    @SecurityRequirements
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
    fun leaveProblemSet(@PathVariable @NotBlank shareCode: String, authentication: Authentication): Mono<String> =
        authentication.monoId().flatMap { problemSetService.removeUser(shareCode, it) }.map { it.shareCode }

    @PostMapping("/{shareCode}/invite")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "cookieAuth")
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
    fun getUserRoles(@PathVariable @NotBlank shareCode: String, authentication: Authentication): Flux<UserNameWithRole> =
        problemSetService.getProblemSetForModerator(shareCode, authentication).flatMapMany {
            problemSetMapper.toUserRoles(it)
        }

    @GetMapping("/{shareCode}/invited-users")
    @SecurityRequirement(name = "cookieAuth")
    fun getInvitedUsers(@PathVariable @NotBlank shareCode: String, authentication: Authentication): Mono<List<String>> =
        problemSetService
            .getProblemSetForModerator(shareCode, authentication)
            .flatMapMany { problemSetMapper.toInvitedUserNames(it) }
            .collectList()

    @PostMapping("/{shareCode}/role")
    @SecurityRequirement(name = "cookieAuth")
    fun changeUserRole(
        @PathVariable @NotBlank shareCode: String,
        @RequestParam @NotBlank username: String,
        @RequestParam @NotNull requestedRole: UserRole,
        authentication: Authentication,
    ): Mono<UserRole> =
        userService
            .findUserByName(username)
            .orNotFound("User $username not found")
            .mapNotNull { it.id }
            .flatMap { userId -> problemSetService.changeUserRole(shareCode, userId, requestedRole, authentication) }

    @PostMapping("/{shareCode}/visibility")
    @SecurityRequirement(name = "cookieAuth")
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
    fun changeProblems(
        @PathVariable @NotBlank shareCode: String,
        @RequestBody @Valid request: ChangeProblemSetProblemsRequest,
        authentication: Authentication,
    ): Mono<ProblemSetDto> =
        problemSetService.changeProblems(shareCode, request.problemKeys, authentication).flatMap {
            problemSetMapper.toDto(it, authentication)
        }
}
