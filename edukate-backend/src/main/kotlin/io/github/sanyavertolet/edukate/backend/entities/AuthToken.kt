package io.github.sanyavertolet.edukate.backend.entities

import java.time.Instant
import java.util.UUID
import org.springframework.data.annotation.Id
import org.springframework.data.domain.Persistable
import org.springframework.data.relational.core.mapping.Table

@Table("auth_tokens")
data class AuthToken(
    @Id val token: UUID,
    val userId: Long,
    val type: AuthTokenType,
    val expiresAt: Instant,
    val email: String,
    val createdAt: Instant = Instant.now(),
) : Persistable<UUID> {
    override fun getId(): UUID = token

    override fun isNew(): Boolean = true
}
