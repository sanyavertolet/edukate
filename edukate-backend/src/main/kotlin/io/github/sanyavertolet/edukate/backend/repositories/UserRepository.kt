package io.github.sanyavertolet.edukate.backend.repositories

import io.github.sanyavertolet.edukate.backend.entities.User
import io.github.sanyavertolet.edukate.common.users.UserStatus
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
interface UserRepository : ReactiveMongoRepository<User, String> {
    fun findByName(name: String): Mono<User>

    fun findAllByStatus(status: UserStatus): Flux<User>
}
