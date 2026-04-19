package io.github.sanyavertolet.edukate.backend.mappers

import io.github.sanyavertolet.edukate.backend.dtos.ProblemSetDto
import io.github.sanyavertolet.edukate.backend.dtos.ProblemSetMetadata
import io.github.sanyavertolet.edukate.backend.dtos.UserNameWithRole
import io.github.sanyavertolet.edukate.backend.entities.ProblemSet
import io.github.sanyavertolet.edukate.backend.repositories.ProblemSetProblemRepository
import io.github.sanyavertolet.edukate.backend.services.ProblemService
import io.github.sanyavertolet.edukate.backend.services.UserService
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Component
class ProblemSetMapper(
    private val problemService: ProblemService,
    private val problemMapper: ProblemMapper,
    private val userService: UserService,
    private val problemSetProblemRepository: ProblemSetProblemRepository,
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

    fun toMetadata(problemSet: ProblemSet): Mono<ProblemSetMetadata> =
        problemSetProblemRepository.findByProblemSetIdOrderByPosition(requireNotNull(problemSet.id)).count().flatMap { size
            ->
            adminNames(problemSet).map { admins ->
                ProblemSetMetadata(
                    name = problemSet.name,
                    description = problemSet.description,
                    admins = admins,
                    shareCode = problemSet.shareCode,
                    isPublic = problemSet.isPublic,
                    size = size,
                )
            }
        }

    fun toUserRoles(problemSet: ProblemSet): Flux<UserNameWithRole> =
        Flux.fromIterable(problemSet.userIdRoleMap.keys)
            .flatMap { userId -> userService.findUserById(userId).map { user -> userId to user } }
            .map { (userId, user) -> UserNameWithRole(user.name, problemSet.userIdRoleMap.getValue(userId)) }

    fun toInvitedUserNames(problemSet: ProblemSet): Flux<String> =
        Flux.fromIterable(problemSet.invitedUserIds).flatMap { userService.findUserById(it) }.map { it.name }

    private fun adminNames(problemSet: ProblemSet): Mono<List<String>> =
        Flux.fromIterable(problemSet.getAdminIds()).flatMap { userService.findUserById(it) }.map { it.name }.collectList()
}
