package io.github.sanyavertolet.edukate.auth.services

import io.github.sanyavertolet.edukate.auth.AuthFixtures
import io.github.sanyavertolet.edukate.common.users.UserRole
import io.github.sanyavertolet.edukate.common.users.UserStatus
import io.jsonwebtoken.JwtException
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class JwtTokenServiceTest {
    private val service = JwtTokenService(AuthFixtures.SECRET, AuthFixtures.EXPIRATION_SECONDS)

    @Test
    fun `generateToken returns non-blank JWT`() {
        val token = service.generateToken(AuthFixtures.userDetails())

        assertThat(token).isNotBlank()
        assertThat(token.split(".")).hasSize(3)
    }

    @Test
    fun `token round-trip preserves id`() {
        val details = AuthFixtures.userDetails()
        val token = service.generateToken(details)

        val parsed = service.getUserDetailsFromToken(token)

        assertThat(parsed).isNotNull
        assertThat(parsed!!.id).isEqualTo(details.id)
    }

    @Test
    fun `token round-trip preserves name`() {
        val details = AuthFixtures.userDetails()
        val token = service.generateToken(details)

        val parsed = service.getUserDetailsFromToken(token)

        assertThat(parsed!!.username).isEqualTo(details.username)
    }

    @Test
    fun `token round-trip preserves USER role`() {
        val details = AuthFixtures.userDetails(roles = setOf(UserRole.USER))
        val token = service.generateToken(details)

        val parsed = service.getUserDetailsFromToken(token)

        assertThat(parsed!!.roles).containsExactly(UserRole.USER)
    }

    @Test
    fun `token round-trip preserves multiple roles`() {
        val details = AuthFixtures.userDetails(roles = setOf(UserRole.ADMIN, UserRole.MODERATOR))
        val token = service.generateToken(details)

        val parsed = service.getUserDetailsFromToken(token)

        assertThat(parsed!!.roles).containsExactlyInAnyOrder(UserRole.ADMIN, UserRole.MODERATOR)
    }

    @Test
    fun `token round-trip preserves status`() {
        val details = AuthFixtures.userDetails(status = UserStatus.PENDING)
        val token = service.generateToken(details)

        val parsed = service.getUserDetailsFromToken(token)

        assertThat(parsed!!.status).isEqualTo(UserStatus.PENDING)
    }

    @Test
    fun `getUserDetailsFromToken with expired token returns null`() {
        val expiredService = JwtTokenService(AuthFixtures.SECRET, 0L)
        val token = expiredService.generateToken(AuthFixtures.userDetails())

        val result = expiredService.getUserDetailsFromToken(token)

        assertThat(result).isNull()
    }

    @Test
    fun `getUserDetailsFromToken with tampered signature throws`() {
        val token = service.generateToken(AuthFixtures.userDetails())
        val tampered = token.dropLast(4) + "XXXX"

        assertThatThrownBy { service.getUserDetailsFromToken(tampered) }.isInstanceOf(JwtException::class.java)
    }

    @Test
    fun `getUserDetailsFromToken with wrong key throws`() {
        val otherService = JwtTokenService("b".repeat(32), AuthFixtures.EXPIRATION_SECONDS)
        val token = service.generateToken(AuthFixtures.userDetails())

        assertThatThrownBy { otherService.getUserDetailsFromToken(token) }.isInstanceOf(JwtException::class.java)
    }

    @Test
    fun `generated token is not expired immediately`() {
        val token = service.generateToken(AuthFixtures.userDetails())

        val result = service.getUserDetailsFromToken(token)

        assertThat(result).isNotNull
    }
}
