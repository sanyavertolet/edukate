package io.github.sanyavertolet.edukate.backend.mappers

import io.github.sanyavertolet.edukate.backend.dtos.ProblemSetDto
import io.github.sanyavertolet.edukate.backend.dtos.ProblemSetMetadata
import io.github.sanyavertolet.edukate.backend.dtos.UserNameWithRole
import io.github.sanyavertolet.edukate.backend.entities.ProblemSet
import io.github.sanyavertolet.edukate.backend.repositories.ProblemProgressRepository
import io.github.sanyavertolet.edukate.backend.repositories.ProblemSetProblemRepository
import io.github.sanyavertolet.edukate.backend.services.ProblemService
import io.github.sanyavertolet.edukate.backend.services.UserService
import io.github.sanyavertolet.edukate.common.utils.id
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.util.function.component1
import reactor.kotlin.core.util.function.component2

@Component
class ProblemSetMapper(
    private val problemService: ProblemService,
    private val problemMapper: ProblemMapper,
    private val userService: UserService,
    private val problemSetProblemRepository: ProblemSetProblemRepository,
    private val problemProgressRepository: ProblemProgressRepository,
) {
    fun toDto(problemSet: ProblemSet, authentication: Authentication?): Mono<ProblemSetDto> =
        problemSetProblemRepository
            .findByProblemSetIdOrderByPosition(requireNotNull(problemSet.id))
            .map { it.problemId }
            .collectList()
            .flatMap { problemIds ->
                problemService
                    .findProblemsByIds(problemIds)
                    .flatMap { problemMapper.toMetadata(it, authentication) }
                    .collectList()
            }
            .flatMap { metadataList ->
                adminNames(problemSet).map { admins ->
                    ProblemSetDto(
                        name = problemSet.name,
                        description = problemSet.description,
                        admins = admins,
                        isPublic = problemSet.isPublic,
                        problems = metadataList,
                        shareCode = problemSet.shareCode,
                    )
                }
            }

    fun toMetadata(problemSet: ProblemSet, authentication: Authentication? = null): Mono<ProblemSetMetadata> =
        problemSetProblemRepository.findByProblemSetIdOrderByPosition(requireNotNull(problemSet.id)).count().flatMap { size
            ->
            val userId = authentication.id()
            val solvedCountMono =
                if (userId != null) {
                    problemProgressRepository.countSolvedInProblemSet(requireNotNull(problemSet.id), userId)
                } else {
                    Mono.just(0L)
                }

            Mono.zip(adminNames(problemSet), solvedCountMono).map { (admins, solvedCount) ->
                ProblemSetMetadata(
                    name = problemSet.name,
                    description = problemSet.description,
                    admins = admins,
                    shareCode = problemSet.shareCode,
                    isPublic = problemSet.isPublic,
                    size = size,
                    solvedCount = solvedCount,
                )
            }
        }

    fun toUserRoles(problemSet: ProblemSet): Flux<UserNameWithRole> =
        Flux.fromIterable(problemSet.userIdRoleMap.keys)
            .flatMap { userId -> userService.findUserById(userId).map { user -> userId to user } }
            .map { (userId, user) -> UserNameWithRole(user.name, problemSet.userIdRoleMap.getValue(userId)) }
            .collectSortedList(compareByDescending<UserNameWithRole> { it.role }.thenBy { it.name })
            .flatMapMany { Flux.fromIterable(it) }

    fun toInvitedUserNames(problemSet: ProblemSet): Flux<String> =
        Flux.fromIterable(problemSet.invitedUserIds).flatMap { userService.findUserById(it) }.map { it.name }

    private fun adminNames(problemSet: ProblemSet): Mono<List<String>> =
        Flux.fromIterable(problemSet.getAdminIds()).flatMap { userService.findUserById(it) }.map { it.name }.collectList()
}
