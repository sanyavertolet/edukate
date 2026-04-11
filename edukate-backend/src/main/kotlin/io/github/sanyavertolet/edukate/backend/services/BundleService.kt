package io.github.sanyavertolet.edukate.backend.services

import io.github.sanyavertolet.edukate.backend.dtos.BundleDto
import io.github.sanyavertolet.edukate.backend.dtos.BundleMetadata
import io.github.sanyavertolet.edukate.backend.dtos.CreateBundleRequest
import io.github.sanyavertolet.edukate.backend.dtos.UserNameWithRole
import io.github.sanyavertolet.edukate.backend.entities.Bundle
import io.github.sanyavertolet.edukate.backend.permissions.BundlePermissionEvaluator
import io.github.sanyavertolet.edukate.backend.repositories.BundleRepository
import io.github.sanyavertolet.edukate.common.users.UserRole
import io.github.sanyavertolet.edukate.common.utils.id
import io.github.sanyavertolet.edukate.common.utils.monoId
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpStatus
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

@Service
@Suppress("TooManyFunctions")
class BundleService(
    private val bundleRepository: BundleRepository,
    private val shareCodeGenerator: ShareCodeGenerator,
    private val problemService: ProblemService,
    private val bundlePermissionEvaluator: BundlePermissionEvaluator,
    private val userService: UserService,
) {
    fun findBundleByShareCode(shareCode: String): Mono<Bundle> =
        bundleRepository
            .findBundleByShareCode(shareCode)
            .switchIfEmpty(ResponseStatusException(HttpStatus.NOT_FOUND, "Bundle [$shareCode] not found").toMono())

    fun getOwnedBundles(pageable: PageRequest, authentication: Authentication): Flux<Bundle> =
        authentication.monoId().flatMapMany { userId ->
            bundleRepository.findBundlesByUserRoleIn(userId, listOf(UserRole.ADMIN), pageable)
        }

    fun getJoinedBundles(pageable: PageRequest, authentication: Authentication): Flux<Bundle> =
        authentication.monoId().flatMapMany { userId ->
            bundleRepository.findBundlesByUserRoleIn(userId, UserRole.anyRole(), pageable)
        }

    fun getPublicBundles(pageable: PageRequest): Flux<Bundle> = bundleRepository.findBundlesByIsPublic(true, pageable)

    fun createBundle(createBundleRequest: CreateBundleRequest, authentication: Authentication): Mono<Bundle> =
        authentication
            .monoId()
            .map { userId -> Bundle.fromCreateRequest(createBundleRequest, userId, shareCodeGenerator.generateShareCode()) }
            .flatMap { bundleRepository.save(it) }

    @Transactional
    fun joinUser(shareCode: String, userId: String): Mono<Bundle> =
        findBundleByShareCode(shareCode)
            .filter { bundlePermissionEvaluator.hasJoinPermission(it, userId) }
            .switchIfEmpty(
                ResponseStatusException(HttpStatus.FORBIDDEN, "To join this bundle, you should be invited").toMono()
            )
            .filter { !it.isUserInBundle(userId) }
            .switchIfEmpty(
                ResponseStatusException(HttpStatus.BAD_REQUEST, "You have already joined the bundle [$shareCode]").toMono()
            )
            .map { it.withJoinedUser(userId, UserRole.USER) }
            .flatMap { bundleRepository.save(it) }

    @Transactional
    fun removeUser(shareCode: String, userId: String): Mono<Bundle> =
        findBundleByShareCode(shareCode)
            .filter { it.isUserInBundle(userId) }
            .switchIfEmpty(ResponseStatusException(HttpStatus.BAD_REQUEST, "User is not in bundle").toMono())
            .filter { !it.isAdmin(userId) || it.getAdminIds().size > 1 }
            .switchIfEmpty(
                ResponseStatusException(HttpStatus.BAD_REQUEST, "Last admin should delete bundle, not leave it").toMono()
            )
            .map { it.withoutUser(userId) }
            .flatMap { bundleRepository.save(it) }

    @Transactional
    fun inviteUser(shareCode: String, inviterId: String, inviteeId: String): Mono<Bundle> =
        findBundleByShareCode(shareCode)
            .filter { bundlePermissionEvaluator.hasInvitePermission(it, inviterId) }
            .switchIfEmpty(ResponseStatusException(HttpStatus.FORBIDDEN, "Not enough permissions to invite").toMono())
            .filter { !it.isUserInBundle(inviteeId) }
            .switchIfEmpty(ResponseStatusException(HttpStatus.BAD_REQUEST, "User is already in bundle").toMono())
            .map { it.withInvitedUser(inviteeId) }
            .flatMap { bundleRepository.save(it) }

    @Transactional
    fun expireInvite(shareCode: String, inviterId: String, inviteeId: String): Mono<Bundle> =
        findBundleByShareCode(shareCode)
            .filter { bundlePermissionEvaluator.hasInvitePermission(it, inviterId) }
            .switchIfEmpty(ResponseStatusException(HttpStatus.FORBIDDEN, "Not enough permissions to expire invite").toMono())
            .filter { it.isUserInvited(inviteeId) }
            .switchIfEmpty(ResponseStatusException(HttpStatus.BAD_REQUEST, "User is not invited to this bundle").toMono())
            .map { it.withoutInvitedUser(inviteeId) }
            .flatMap { bundleRepository.save(it) }

    @Transactional
    fun getBundleUsers(shareCode: String, authentication: Authentication): Flux<UserNameWithRole> =
        findBundleByShareCode(shareCode)
            .filter {
                val requesterId = requireNotNull(authentication.id())
                bundlePermissionEvaluator.hasRole(it, requesterId, UserRole.MODERATOR)
            }
            .flatMapMany { bundle ->
                val userIdRoleMap = bundle.userIdRoleMap
                userService.findUsersByIds(userIdRoleMap.keys).map { user ->
                    UserNameWithRole(user.name, userIdRoleMap.getValue(requireNotNull(user.id)))
                }
            }

    @Transactional
    fun getBundleInvitedUsers(shareCode: String, authentication: Authentication): Flux<String> =
        findBundleByShareCode(shareCode)
            .filter {
                val requesterId = requireNotNull(authentication.id())
                bundlePermissionEvaluator.hasRole(it, requesterId, UserRole.MODERATOR)
            }
            .flatMapMany { bundle -> userService.findUsersByIds(bundle.invitedUserIds).map { it.name } }

    @Transactional
    fun changeUserRole(
        shareCode: String,
        userId: String,
        requestedRole: UserRole,
        authentication: Authentication,
    ): Mono<UserRole> =
        findBundleByShareCode(shareCode)
            .filter { it.isUserInBundle(userId) }
            .switchIfEmpty(ResponseStatusException(HttpStatus.NOT_FOUND, "Target user not found in bundle").toMono())
            .filter {
                val requesterId = requireNotNull(authentication.id())
                bundlePermissionEvaluator.hasChangeRolePermission(it, requesterId, userId, requestedRole)
            }
            .switchIfEmpty(
                ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot set $userId role to be $requestedRole").toMono()
            )
            .map { it.withUserRole(userId, requestedRole) }
            .flatMap { bundleRepository.save(it) }
            .thenReturn(requestedRole)

    @Transactional
    fun declineInvite(shareCode: String, authentication: Authentication): Mono<Bundle> =
        findBundleByShareCode(shareCode)
            .filter { it.isUserInvited(requireNotNull(authentication.id())) }
            .switchIfEmpty(
                ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "You cannot decline the invitation as nobody has invited you yet",
                    )
                    .toMono()
            )
            .map { it.withoutInvitedUser(requireNotNull(authentication.id())) }
            .flatMap { bundleRepository.save(it) }

    @Transactional
    fun changeVisibility(shareCode: String, isPublic: Boolean, authentication: Authentication): Mono<Bundle> =
        findBundleByShareCode(shareCode)
            .filter { bundlePermissionEvaluator.hasRole(it, UserRole.MODERATOR, authentication) }
            .switchIfEmpty(
                ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot change visibility due to lack of permissions").toMono()
            )
            .map { it.withVisibility(isPublic) }
            .flatMap { bundleRepository.save(it) }

    @Transactional
    fun changeProblems(shareCode: String, problemIds: List<String>, authentication: Authentication): Mono<Bundle> =
        shareCode
            .toMono()
            .filter { problemIds.isNotEmpty() }
            .switchIfEmpty(ResponseStatusException(HttpStatus.BAD_REQUEST, "Bundle problem list cannot be empty").toMono())
            .flatMap { findBundleByShareCode(it) }
            .filter { bundlePermissionEvaluator.hasRole(it, UserRole.MODERATOR, authentication) }
            .switchIfEmpty(
                ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot change problem list due to lack of permissions")
                    .toMono()
            )
            .map { it.withProblemIds(problemIds) }
            .flatMap { bundleRepository.save(it) }

    fun prepareDto(bundle: Bundle, authentication: Authentication?): Mono<BundleDto> =
        problemService
            .findProblemsByIds(bundle.problemIds)
            .flatMap { problem -> problemService.prepareMetadata(problem, authentication) }
            .collectList()
            .flatMap { metadataList -> getAdmins(bundle).map { admins -> bundle.toDto(metadataList, admins) } }

    fun prepareMetadata(bundle: Bundle): Mono<BundleMetadata> = getAdmins(bundle).map { bundle.toBundleMetadata(it) }

    private fun getAdmins(bundle: Bundle): Mono<List<String>> =
        userService.findUsersByIds(bundle.getAdminIds()).map { it.name }.collectList()
}
