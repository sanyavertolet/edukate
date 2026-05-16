package io.github.sanyavertolet.edukate.backend.repositories

import io.github.sanyavertolet.edukate.backend.entities.AuthToken
import io.github.sanyavertolet.edukate.backend.entities.AuthTokenType
import java.time.Instant
import java.util.UUID
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
interface AuthTokenRepository : ReactiveCrudRepository<AuthToken, UUID> {
    fun findByTokenAndType(token: UUID, type: AuthTokenType): Mono<AuthToken>

    fun deleteAllByUserIdAndType(userId: Long, type: AuthTokenType): Mono<Void>

    fun deleteAllByExpiresAtBefore(cutoff: Instant): Mono<Void>
}
