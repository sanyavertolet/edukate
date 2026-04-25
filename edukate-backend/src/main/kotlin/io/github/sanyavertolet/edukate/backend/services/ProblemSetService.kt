package io.github.sanyavertolet.edukate.backend.services

import io.github.sanyavertolet.edukate.backend.dtos.CreateProblemSetRequest
import io.github.sanyavertolet.edukate.backend.entities.ProblemSet
import io.github.sanyavertolet.edukate.backend.entities.ProblemSetProblem
import io.github.sanyavertolet.edukate.backend.permissions.ProblemSetPermissionEvaluator
import io.github.sanyavertolet.edukate.backend.repositories.ProblemRepository
import io.github.sanyavertolet.edukate.backend.repositories.ProblemSetProblemRepository
import io.github.sanyavertolet.edukate.backend.repositories.ProblemSetRepository
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
) {
    @Cacheable(key = "#shareCode") fun findByShareCode(shareCode: String): Mono<ProblemSet> = loadProblemSet(shareCode)

    fun getOwnedProblemSets(pageable: PageRequest, authentication: Authentication): Flux<ProblemSet> =
        authentication.monoId().flatMapMany { userId -> problemSetRepository.findOwnedByUserId(userId, pageable) }

    fun getJoinedProblemSets(pageable: PageRequest, authentication: Authentication): Flux<ProblemSet> =
        authentication.monoId().flatMapMany { userId -> problemSetRepository.findJoinedByUserId(userId, pageable) }

    fun getPublicProblemSets(pageable: PageRequest): Flux<ProblemSet> = problemSetRepository.findByIsPublic(true, pageable)

    fun createProblemSet(request: CreateProblemSetRequest, authentication: Authentication): Mono<ProblemSet> =
        authentication.monoId().flatMap { userId ->
            resolveProblemKeys(request.problemKeys).flatMap { problemIds ->
                Mono.defer {
                        val problemSet =
                            ProblemSet(
                                name = request.name,
                                description = request.description,
                                isPublic = request.isPublic,
                                shareCode = shareCodeGenerator.generateShareCode(),
                                userIdRoleMap = mapOf(userId to UserRole.ADMIN),
                            )
                        problemSetRepository.save(problemSet)
                    }
                    .retryWhen(Retry.max(SHARE_CODE_RETRY_LIMIT).filter { it is DuplicateKeyException })
                    .flatMap { saved ->
                        val entries =
                            problemIds.mapIndexed { index, problemId ->
                                ProblemSetProblem(requireNotNull(saved.id), problemId, index)
                            }
                        problemSetProblemRepository.saveAll(entries).then(Mono.just(saved))
                    }
            }
        }

    @CacheEvict(key = "#shareCode")
    @Transactional
    fun removeUser(shareCode: String, userId: Long): Mono<ProblemSet> =
        mutate(shareCode, { it.withoutUser(userId) }) { mono ->
            mono
                .badRequestIf("User is not in problem set") { !it.isUserInProblemSet(userId) }
                .badRequestIf("Last admin should delete problem set, not leave it") {
                    it.isAdmin(userId) && it.getAdminIds().size == 1
                }
        }

    @CacheEvict(key = "#shareCode")
    @Transactional
    fun removeUserByModerator(shareCode: String, targetUserId: Long, authentication: Authentication): Mono<ProblemSet> =
        mutate(shareCode, { it.withoutUser(targetUserId) }) { mono ->
            mono
                .notFoundIf("Target user not found in problem set") { !it.isUserInProblemSet(targetUserId) }
                .forbiddenIf("Not enough permissions to remove user") {
                    !problemSetPermissionEvaluator.hasRemovePermission(it, requireNotNull(authentication.id()), targetUserId)
                }
        }

    @CacheEvict(key = "#shareCode")
    @Transactional
    fun inviteUser(shareCode: String, inviterId: Long, inviteeId: Long): Mono<ProblemSet> =
        mutate(shareCode, { it.withInvitedUser(inviteeId) }) { mono ->
            mono
                .forbiddenIf("Not enough permissions to invite") {
                    !problemSetPermissionEvaluator.hasInvitePermission(it, inviterId)
                }
                .badRequestIf("User is already in problem set") { it.isUserInProblemSet(inviteeId) }
        }

    @CacheEvict(key = "#shareCode")
    @Transactional
    fun expireInvite(shareCode: String, inviterId: Long, inviteeId: Long): Mono<ProblemSet> =
        mutate(shareCode, { it.withoutInvitedUser(inviteeId) }) { mono ->
            mono
                .forbiddenIf("Not enough permissions to expire invite") {
                    !problemSetPermissionEvaluator.hasInvitePermission(it, inviterId)
                }
                .badRequestIf("User is not invited to this problem set") { !it.isUserInvited(inviteeId) }
        }

    @CacheEvict(key = "#shareCode")
    @Transactional
    fun reactToInvite(shareCode: String, accepted: Boolean, authentication: Authentication): Mono<ProblemSet> {
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
    fun getProblemSetForModerator(shareCode: String, authentication: Authentication): Mono<ProblemSet> =
        loadProblemSet(shareCode).forbiddenIf("You are not allowed to view this problem set") {
            !problemSetPermissionEvaluator.hasRole(it, UserRole.MODERATOR, authentication)
        }

    @CacheEvict(key = "#shareCode")
    @Transactional
    fun changeUserRole(
        shareCode: String,
        userId: Long,
        requestedRole: UserRole,
        authentication: Authentication,
    ): Mono<UserRole> =
        mutate(shareCode, { it.withUserRole(userId, requestedRole) }) { mono ->
                mono
                    .notFoundIf("Target user not found in problem set") { !it.isUserInProblemSet(userId) }
                    .forbiddenIf("Cannot set $userId role to be $requestedRole") {
                        !problemSetPermissionEvaluator.hasChangeRolePermission(
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
    fun changeVisibility(shareCode: String, isPublic: Boolean, authentication: Authentication): Mono<ProblemSet> =
        mutate(shareCode, { it.withVisibility(isPublic) }) { mono ->
            mono.forbiddenIf("Cannot change visibility due to lack of permissions") {
                !problemSetPermissionEvaluator.hasRole(it, UserRole.MODERATOR, authentication)
            }
        }

    @CacheEvict(key = "#shareCode")
    @Transactional
    fun changeProblems(shareCode: String, problemKeys: List<String>, authentication: Authentication): Mono<ProblemSet> =
        loadProblemSet(shareCode)
            .forbiddenIf("Cannot change problem list due to lack of permissions") {
                !problemSetPermissionEvaluator.hasRole(it, UserRole.MODERATOR, authentication)
            }
            .flatMap { ps ->
                resolveProblemKeys(problemKeys).flatMap { problemIds ->
                    val psId = requireNotNull(ps.id)
                    problemSetProblemRepository
                        .deleteByProblemSetId(psId)
                        .then(
                            problemSetProblemRepository
                                .saveAll(problemIds.mapIndexed { index, pid -> ProblemSetProblem(psId, pid, index) })
                                .then(Mono.just(ps))
                        )
                }
            }

    private fun resolveProblemKeys(keys: List<String>): Mono<List<Long>> =
        problemRepository.findByKeyIn(keys).collectList().map { problems ->
            val keyToId = problems.associate { it.key to requireNotNull(it.id) }
            keys.map { key -> requireNotNull(keyToId[key]) { "Problem with key $key not found" } }
        }

    private fun loadProblemSet(shareCode: String): Mono<ProblemSet> =
        problemSetRepository.findByShareCode(shareCode).orNotFound("ProblemSet [$shareCode] not found")

    private fun mutate(
        shareCode: String,
        transform: (ProblemSet) -> ProblemSet,
        validate: (Mono<ProblemSet>) -> Mono<ProblemSet>,
    ): Mono<ProblemSet> = validate(loadProblemSet(shareCode)).map { transform(it) }.flatMap { problemSetRepository.save(it) }

    companion object {
        private const val SHARE_CODE_RETRY_LIMIT = 3L
    }
}
