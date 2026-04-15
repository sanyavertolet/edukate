package io.github.sanyavertolet.edukate.backend.services

import io.github.sanyavertolet.edukate.backend.dtos.CreateBundleRequest
import io.github.sanyavertolet.edukate.backend.entities.Bundle
import io.github.sanyavertolet.edukate.backend.permissions.BundlePermissionEvaluator
import io.github.sanyavertolet.edukate.backend.repositories.BundleRepository
import io.github.sanyavertolet.edukate.common.users.UserRole
import io.github.sanyavertolet.edukate.common.utils.badRequestIf
import io.github.sanyavertolet.edukate.common.utils.forbiddenIf
import io.github.sanyavertolet.edukate.common.utils.id
import io.github.sanyavertolet.edukate.common.utils.monoId
import io.github.sanyavertolet.edukate.common.utils.notFoundIf
import io.github.sanyavertolet.edukate.common.utils.orNotFound
import org.springframework.cache.annotation.CacheConfig
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.domain.PageRequest
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Service
@CacheConfig(cacheNames = ["bundles"])
@Suppress("TooManyFunctions")
class BundleService(
    private val bundleRepository: BundleRepository,
    private val shareCodeGenerator: ShareCodeGenerator,
    private val bundlePermissionEvaluator: BundlePermissionEvaluator,
) {
    @Cacheable(key = "#shareCode") fun findBundleByShareCode(shareCode: String): Mono<Bundle> = loadBundle(shareCode)

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

    @CacheEvict(key = "#shareCode")
    @Transactional
    fun removeUser(shareCode: String, userId: String): Mono<Bundle> =
        mutate(shareCode, { it.withoutUser(userId) }) { mono ->
            mono
                .badRequestIf("User is not in bundle") { !it.isUserInBundle(userId) }
                .badRequestIf("Last admin should delete bundle, not leave it") {
                    it.isAdmin(userId) && it.getAdminIds().size == 1
                }
        }

    @CacheEvict(key = "#shareCode")
    @Transactional
    fun inviteUser(shareCode: String, inviterId: String, inviteeId: String): Mono<Bundle> =
        mutate(shareCode, { it.withInvitedUser(inviteeId) }) { mono ->
            mono
                .forbiddenIf("Not enough permissions to invite") {
                    !bundlePermissionEvaluator.hasInvitePermission(it, inviterId)
                }
                .badRequestIf("User is already in bundle") { it.isUserInBundle(inviteeId) }
        }

    @CacheEvict(key = "#shareCode")
    @Transactional
    fun expireInvite(shareCode: String, inviterId: String, inviteeId: String): Mono<Bundle> =
        mutate(shareCode, { it.withoutInvitedUser(inviteeId) }) { mono ->
            mono
                .forbiddenIf("Not enough permissions to expire invite") {
                    !bundlePermissionEvaluator.hasInvitePermission(it, inviterId)
                }
                .badRequestIf("User is not invited to this bundle") { !it.isUserInvited(inviteeId) }
        }

    @CacheEvict(key = "#shareCode")
    @Transactional
    fun reactToInvite(shareCode: String, accepted: Boolean, authentication: Authentication): Mono<Bundle> {
        val userId = requireNotNull(authentication.id())
        return mutate(
            shareCode,
            { if (accepted) it.withJoinedUser(userId, UserRole.USER) else it.withoutInvitedUser(userId) },
        ) { mono ->
            mono.forbiddenIf("You cannot react to the invitation as nobody has invited you yet") {
                !it.isUserInvited(userId)
            }
        }
    }

    @Transactional(readOnly = true)
    fun getBundleForModerator(shareCode: String, authentication: Authentication): Mono<Bundle> =
        loadBundle(shareCode).forbiddenIf("You are not allowed to view this bundle") {
            !bundlePermissionEvaluator.hasRole(it, UserRole.MODERATOR, authentication)
        }

    @CacheEvict(key = "#shareCode")
    @Transactional
    fun changeUserRole(
        shareCode: String,
        userId: String,
        requestedRole: UserRole,
        authentication: Authentication,
    ): Mono<UserRole> =
        mutate(shareCode, { it.withUserRole(userId, requestedRole) }) { mono ->
                mono
                    .notFoundIf("Target user not found in bundle") { !it.isUserInBundle(userId) }
                    .forbiddenIf("Cannot set $userId role to be $requestedRole") {
                        !bundlePermissionEvaluator.hasChangeRolePermission(
                            it,
                            requireNotNull(authentication.id()),
                            userId,
                            requestedRole,
                        )
                    }
            }
            .thenReturn(requestedRole)

    @CacheEvict(key = "#shareCode")
    @Transactional
    fun changeVisibility(shareCode: String, isPublic: Boolean, authentication: Authentication): Mono<Bundle> =
        mutate(shareCode, { it.withVisibility(isPublic) }) { mono ->
            mono.forbiddenIf("Cannot change visibility due to lack of permissions") {
                !bundlePermissionEvaluator.hasRole(it, UserRole.MODERATOR, authentication)
            }
        }

    @CacheEvict(key = "#shareCode")
    @Transactional
    fun changeProblems(shareCode: String, problemIds: List<String>, authentication: Authentication): Mono<Bundle> =
        mutate(shareCode, { it.withProblemIds(problemIds) }) { mono ->
            mono.forbiddenIf("Cannot change problem list due to lack of permissions") {
                !bundlePermissionEvaluator.hasRole(it, UserRole.MODERATOR, authentication)
            }
        }

    private fun loadBundle(shareCode: String): Mono<Bundle> =
        bundleRepository.findBundleByShareCode(shareCode).orNotFound("Bundle [$shareCode] not found")

    private fun mutate(
        shareCode: String,
        transform: (Bundle) -> Bundle,
        validate: (Mono<Bundle>) -> Mono<Bundle>,
    ): Mono<Bundle> = validate(loadBundle(shareCode)).map { transform(it) }.flatMap { bundleRepository.save(it) }
}
