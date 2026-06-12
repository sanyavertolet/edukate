package io.github.sanyavertolet.edukate.backend.services

import io.github.sanyavertolet.edukate.backend.dtos.CreateProblemSetRequest
import io.github.sanyavertolet.edukate.backend.dtos.UpdateProblemSetSettingsRequest
import io.github.sanyavertolet.edukate.backend.entities.ProblemSet
import io.github.sanyavertolet.edukate.backend.entities.ProblemSetProblem
import io.github.sanyavertolet.edukate.backend.permissions.ProblemSetPermissionEvaluator
import io.github.sanyavertolet.edukate.backend.repositories.ProblemRepository
import io.github.sanyavertolet.edukate.backend.repositories.ProblemSetProblemRepository
import io.github.sanyavertolet.edukate.backend.repositories.ProblemSetRepository
import io.github.sanyavertolet.edukate.common.notifications.InviteNotificationCreateRequest
import io.github.sanyavertolet.edukate.common.services.Notifier
import io.github.sanyavertolet.edukate.common.users.UserRole
import io.github.sanyavertolet.edukate.common.utils.badRequestIf
import io.github.sanyavertolet.edukate.common.utils.forbiddenIf
import io.github.sanyavertolet.edukate.common.utils.notFoundIf
import io.github.sanyavertolet.edukate.common.utils.orNotFound
import io.github.sanyavertolet.edukate.common.utils.requireUserId
import org.springframework.cache.annotation.CacheConfig
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.dao.DuplicateKeyException
import org.springframework.data.domain.PageRequest
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.util.retry.Retry

@Service
@CacheConfig(cacheNames = ["problemSets"])
@Suppress("TooManyFunctions")
class ProblemSetService(
    private val problemSetRepository: ProblemSetRepository,
    private val problemSetProblemRepository: ProblemSetProblemRepository,
    private val problemRepository: ProblemRepository,
    private val shareCodeGenerator: ShareCodeGenerator,
    private val problemSetPermissionEvaluator: ProblemSetPermissionEvaluator,
    private val notifier: Notifier,
) {

    // region read

    @Cacheable(key = "#shareCode") fun findByShareCode(shareCode: String): Mono<ProblemSet> = loadProblemSet(shareCode)

    fun findById(id: Long): Mono<ProblemSet> = problemSetRepository.findById(id).orNotFound("ProblemSet [$id] not found")

    fun getMemberProblemSets(
        roles: List<UserRole>?,
        pageable: PageRequest,
        authentication: Authentication,
    ): Flux<ProblemSet> {
        val effectiveRoles = (roles ?: UserRole.entries).map(UserRole::name).toTypedArray()
        return problemSetRepository.findByUserIdAndRoles(authentication.requireUserId(), effectiveRoles, pageable)
    }

    fun getPublicProblemSets(pageable: PageRequest): Flux<ProblemSet> = problemSetRepository.findByIsPublic(true, pageable)

    fun searchMemberProblemSets(
        query: String,
        problemKey: String?,
        pageable: PageRequest,
        authentication: Authentication,
    ): Flux<ProblemSet> =
        problemSetRepository.searchByUserIdAndQuery(authentication.requireUserId(), query, problemKey, pageable)

    @Transactional(readOnly = true)
    fun loadForModerator(shareCode: String, authentication: Authentication): Mono<ProblemSet> =
        loadProblemSet(shareCode).assertModerator(authentication)

    // endregion

    // region create

    fun createProblemSet(request: CreateProblemSetRequest, authentication: Authentication): Mono<ProblemSet> {
        val ownerId = authentication.requireUserId()
        return resolveProblemIds(request.problemKeys).flatMap { problemIds ->
            saveWithUniqueShareCode {
                    ProblemSet(
                        name = request.name,
                        description = request.description.ifBlank { null },
                        isPublic = request.isPublic,
                        shareCode = shareCodeGenerator.generateShareCode(),
                        userIdRoleMap = mapOf(ownerId to UserRole.ADMIN),
                    )
                }
                .flatMap { saved -> persistProblems(saved, problemIds).thenReturn(saved) }
        }
    }

    // endregion

    // region settings

    @CacheEvict(key = "#shareCode")
    @Transactional
    fun updateSettings(
        shareCode: String,
        request: UpdateProblemSetSettingsRequest,
        authentication: Authentication,
    ): Mono<ProblemSet> =
        loadProblemSet(shareCode).assertModerator(authentication).flatMap { ps ->
            val updated = ps.applyFieldUpdates(request)
            val saveStep = if (updated == ps) Mono.just(ps) else problemSetRepository.save(updated)
            if (request.problemKeys == null) saveStep
            else saveStep.flatMap { saved -> replaceProblems(saved, request.problemKeys) }
        }

    // endregion

    // region members

    @CacheEvict(key = "#shareCode")
    @Transactional
    fun setMemberRole(
        shareCode: String,
        targetUserId: Long,
        role: UserRole,
        authentication: Authentication,
    ): Mono<ProblemSet> {
        val requesterId = authentication.requireUserId()
        return loadProblemSet(shareCode)
            .notFoundIf("Target user not found in problem set") { !it.isUserInProblemSet(targetUserId) }
            .forbiddenIf("Cannot set $role for user $targetUserId") {
                !problemSetPermissionEvaluator.hasChangeRolePermission(it, requesterId, targetUserId, role)
            }
            .badRequestIf("Cannot demote the last admin of a problem set") {
                it.isLastAdmin(targetUserId) && role != UserRole.ADMIN
            }
            .flatMap { problemSetRepository.save(it.withUserRole(targetUserId, role)) }
    }

    @CacheEvict(key = "#shareCode")
    @Transactional
    fun removeMember(shareCode: String, targetUserId: Long, authentication: Authentication): Mono<ProblemSet> {
        val requesterId = authentication.requireUserId()
        return loadProblemSet(shareCode)
            .notFoundIf("Target user not found in problem set") { !it.isUserInProblemSet(targetUserId) }
            .forbiddenIf("Not enough permissions to remove user") {
                !problemSetPermissionEvaluator.hasRemovePermission(it, requesterId, targetUserId)
            }
            .badRequestIf("Cannot remove the last admin from a problem set") { it.isLastAdmin(targetUserId) }
            .flatMap { problemSetRepository.save(it.withoutUser(targetUserId)) }
    }

    @CacheEvict(key = "#shareCode")
    @Transactional
    fun leaveProblemSet(shareCode: String, authentication: Authentication): Mono<ProblemSet> {
        val userId = authentication.requireUserId()
        return loadProblemSet(shareCode)
            .badRequestIf("User is not in problem set") { !it.isUserInProblemSet(userId) }
            .badRequestIf("Last admin should delete problem set, not leave it") { it.isLastAdmin(userId) }
            .flatMap { problemSetRepository.save(it.withoutUser(userId)) }
    }

    // endregion

    // region invitations

    @CacheEvict(key = "#shareCode")
    @Transactional
    fun createInvitation(shareCode: String, inviteeId: Long, authentication: Authentication): Mono<ProblemSet> {
        val inviterId = authentication.requireUserId()
        return loadProblemSet(shareCode)
            .forbiddenIf("Not enough permissions to invite") {
                !problemSetPermissionEvaluator.hasInvitePermission(it, inviterId)
            }
            .badRequestIf("User is already in problem set") { it.isUserInProblemSet(inviteeId) }
            .badRequestIf("User is already invited to this problem set") { it.isUserInvited(inviteeId) }
            .flatMap { ps -> problemSetRepository.save(ps.withInvitedUser(inviteeId)) }
            .flatMap { ps -> publishInviteNotification(ps, inviteeId, authentication.name).thenReturn(ps) }
    }

    @CacheEvict(key = "#shareCode")
    @Transactional
    fun revokeInvitation(shareCode: String, inviteeId: Long, authentication: Authentication): Mono<ProblemSet> {
        val requesterId = authentication.requireUserId()
        return loadProblemSet(shareCode)
            .forbiddenIf("Not enough permissions to revoke invite") {
                !problemSetPermissionEvaluator.hasInvitePermission(it, requesterId)
            }
            .badRequestIf("User is not invited to this problem set") { !it.isUserInvited(inviteeId) }
            .flatMap { problemSetRepository.save(it.withoutInvitedUser(inviteeId)) }
    }

    @CacheEvict(key = "#shareCode")
    @Transactional
    fun acceptInvitation(shareCode: String, authentication: Authentication): Mono<ProblemSet> {
        val userId = authentication.requireUserId()
        return loadProblemSet(shareCode)
            .forbiddenIf("You have no pending invitation to this problem set") { !it.isUserInvited(userId) }
            .flatMap { problemSetRepository.save(it.withJoinedUser(userId, UserRole.USER)) }
    }

    @CacheEvict(key = "#shareCode")
    @Transactional
    fun declineInvitation(shareCode: String, authentication: Authentication): Mono<ProblemSet> {
        val userId = authentication.requireUserId()
        return loadProblemSet(shareCode)
            .forbiddenIf("You have no pending invitation to this problem set") { !it.isUserInvited(userId) }
            .flatMap { problemSetRepository.save(it.withoutInvitedUser(userId)) }
    }

    // endregion

    // region private helpers

    fun Mono<ProblemSet>.assertIsModeratorOf(userId: Long): Mono<ProblemSet> =
        forbiddenIf("User is not a moderator of this problem set") {
            !problemSetPermissionEvaluator.hasRole(it, userId, UserRole.MODERATOR)
        }

    private fun Mono<ProblemSet>.assertModerator(authentication: Authentication): Mono<ProblemSet> =
        assertIsModeratorOf(authentication.requireUserId())

    private fun loadProblemSet(shareCode: String): Mono<ProblemSet> =
        problemSetRepository.findByShareCode(shareCode).orNotFound("ProblemSet [$shareCode] not found")

    private fun saveWithUniqueShareCode(factory: () -> ProblemSet): Mono<ProblemSet> =
        Mono.defer { problemSetRepository.save(factory()) }
            .retryWhen(Retry.max(SHARE_CODE_RETRY_LIMIT).filter { it is DuplicateKeyException })

    private fun resolveProblemIds(keys: List<String>): Mono<List<Long>> =
        problemRepository.findByKeyIn(keys).collectList().map { problems ->
            val keyToId = problems.associate { it.key to requireNotNull(it.id) }
            keys.map { key -> requireNotNull(keyToId[key]) { "Problem with key $key not found" } }
        }

    private fun persistProblems(problemSet: ProblemSet, problemIds: List<Long>): Mono<Void> {
        val psId = requireNotNull(problemSet.id)
        val entries = problemIds.mapIndexed { idx, pid -> ProblemSetProblem(psId, pid, idx) }
        return problemSetProblemRepository.saveAll(entries).then()
    }

    private fun replaceProblems(problemSet: ProblemSet, problemKeys: List<String>): Mono<ProblemSet> {
        val psId = requireNotNull(problemSet.id)
        return resolveProblemIds(problemKeys).flatMap { ids ->
            problemSetProblemRepository
                .deleteByProblemSetId(psId)
                .then(persistProblems(problemSet, ids))
                .thenReturn(problemSet)
        }
    }

    private fun publishInviteNotification(problemSet: ProblemSet, inviteeId: Long, inviterName: String): Mono<String> =
        notifier.notify(
            InviteNotificationCreateRequest.from(
                targetUserId = inviteeId,
                inviterName = inviterName,
                problemSetName = problemSet.name,
                problemSetShareCode = problemSet.shareCode,
            )
        )

    // endregion

    companion object {
        private const val SHARE_CODE_RETRY_LIMIT = 3L
    }
}
