package io.github.sanyavertolet.edukate.backend.repositories

import io.github.sanyavertolet.edukate.backend.entities.User
import io.github.sanyavertolet.edukate.common.users.UserStatus
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
interface UserRepository : ReactiveCrudRepository<User, Long> {
    fun findByName(name: String): Mono<User>

    fun findAllByStatus(status: UserStatus): Flux<User>

    @Query("SELECT * FROM users WHERE name LIKE :prefix || '%' ORDER BY name LIMIT :limit")
    fun findByNamePrefix(prefix: String, limit: Int): Flux<User>
}
