package io.github.sanyavertolet.edukate.backend.entities

import java.time.Instant
import java.util.UUID
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class AuthTokenTest {

    @Test
    fun `isNew always returns true`() {
        val token =
            AuthToken(
                token = UUID.randomUUID(),
                userId = 1L,
                type = AuthTokenType.EMAIL_VERIFICATION,
                expiresAt = Instant.now().plusSeconds(3600),
            )
        assertThat(token.isNew()).isTrue()
    }

    @Test
    fun `getId returns the token UUID`() {
        val uuid = UUID.randomUUID()
        val token =
            AuthToken(
                token = uuid,
                userId = 1L,
                type = AuthTokenType.PASSWORD_RESET,
                expiresAt = Instant.now().plusSeconds(3600),
            )
        assertThat(token.id).isEqualTo(uuid)
    }

    @Test
    fun `isNew returns true on repeated calls`() {
        val token = AuthToken(UUID.randomUUID(), 1L, AuthTokenType.EMAIL_VERIFICATION, Instant.now().plusSeconds(100))
        assertThat(token.isNew()).isTrue()
        assertThat(token.isNew()).isTrue()
    }
}
