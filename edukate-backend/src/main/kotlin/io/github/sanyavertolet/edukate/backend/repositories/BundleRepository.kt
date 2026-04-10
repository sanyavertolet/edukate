package io.github.sanyavertolet.edukate.backend.repositories

import io.github.sanyavertolet.edukate.backend.entities.Bundle
import io.github.sanyavertolet.edukate.common.users.UserRole
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.repository.Query
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
interface BundleRepository : ReactiveMongoRepository<Bundle, String> {
    fun findBundlesByIsPublic(isPublic: Boolean, pageable: Pageable): Flux<Bundle>

    fun findBundleByShareCode(shareCode: String): Mono<Bundle>

    @Query($$"{ 'userIdRoleMap.?0': { $in: ?1 } }")
    fun findBundlesByUserRoleIn(userId: String, roles: Collection<UserRole>, pageable: Pageable): Flux<Bundle>
}
