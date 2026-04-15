package io.github.sanyavertolet.edukate.backend.mappers

import io.github.sanyavertolet.edukate.backend.dtos.BundleDto
import io.github.sanyavertolet.edukate.backend.dtos.BundleMetadata
import io.github.sanyavertolet.edukate.backend.dtos.UserNameWithRole
import io.github.sanyavertolet.edukate.backend.entities.Bundle
import io.github.sanyavertolet.edukate.backend.services.ProblemService
import io.github.sanyavertolet.edukate.backend.services.UserService
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Component
class BundleMapper(
    private val problemService: ProblemService,
    private val problemMapper: ProblemMapper,
    private val userService: UserService,
) {
    fun toDto(bundle: Bundle, authentication: Authentication?): Mono<BundleDto> =
        problemService
            .findProblemsByIds(bundle.problemIds)
            .flatMap { problemMapper.toMetadata(it, authentication) }
            .collectList()
            .flatMap { metadataList -> adminNames(bundle).map { admins -> bundle.toDto(metadataList, admins) } }

    fun toMetadata(bundle: Bundle): Mono<BundleMetadata> = adminNames(bundle).map { bundle.toBundleMetadata(it) }

    fun toUserRoles(bundle: Bundle): Flux<UserNameWithRole> =
        Flux.fromIterable(bundle.userIdRoleMap.keys)
            .flatMap { userId -> userService.findUserById(userId) }
            .map { user -> UserNameWithRole(user.name, bundle.userIdRoleMap.getValue(requireNotNull(user.id))) }

    fun toInvitedUserNames(bundle: Bundle): Flux<String> =
        Flux.fromIterable(bundle.invitedUserIds).flatMap { userService.findUserById(it) }.map { it.name }

    private fun adminNames(bundle: Bundle): Mono<List<String>> =
        Flux.fromIterable(bundle.getAdminIds()).flatMap { userService.findUserById(it) }.map { it.name }.collectList()
}
