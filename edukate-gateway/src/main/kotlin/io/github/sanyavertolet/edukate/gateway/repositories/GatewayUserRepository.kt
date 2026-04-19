package io.github.sanyavertolet.edukate.gateway.repositories

import io.github.sanyavertolet.edukate.gateway.entities.GatewayUser
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
interface GatewayUserRepository : ReactiveCrudRepository<GatewayUser, Long> {
    fun findByName(name: String): Mono<GatewayUser>
}
