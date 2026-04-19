package io.github.sanyavertolet.edukate.common.users

import io.github.sanyavertolet.edukate.common.CommonFixtures
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken

class EdukateUserDetailsTest {

    @Test
    fun `constructor from UserCredentials copies all fields`() {
        val creds = CommonFixtures.userCredentials(roles = setOf(UserRole.ADMIN))
        val details = EdukateUserDetails(creds)

        assertThat(details.id).isEqualTo(creds.id)
        assertThat(details.username).isEqualTo(creds.username)
        assertThat(details.roles).containsExactly(UserRole.ADMIN)
        assertThat(details.status).isEqualTo(creds.status)
        assertThat(details.password).isEqualTo(creds.encodedPassword)
    }

    @Test
    fun `constructor from UserCredentials with null id throws`() {
        val creds = UserCredentials.newUser(CommonFixtures.USER_NAME, CommonFixtures.ENCODED_PASSWORD, CommonFixtures.EMAIL)
        assertThatThrownBy { EdukateUserDetails(creds) }.isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun `getAuthorities maps roles to granted authorities`() {
        val details = CommonFixtures.userDetails(roles = setOf(UserRole.USER, UserRole.ADMIN))
        val authorityNames = details.authorities.map { it.authority }

        assertThat(authorityNames).containsExactlyInAnyOrder("ROLE_USER", "ROLE_ADMIN")
    }

    @Test
    fun `getAuthorities returns empty collection for no roles`() {
        assertThat(CommonFixtures.userDetails(roles = emptySet()).authorities).isEmpty()
    }

    @Test
    fun `isEnabled reflects UserStatus`() {
        assertThat(CommonFixtures.userDetails(status = UserStatus.ACTIVE).isEnabled).isTrue()
        assertThat(CommonFixtures.userDetails(status = UserStatus.PENDING).isEnabled).isFalse()
        assertThat(CommonFixtures.userDetails(status = UserStatus.DELETED).isEnabled).isFalse()
    }

    @Test
    fun `eraseCredentials clears password to empty string`() {
        val details = CommonFixtures.userDetails(token = "secret-token")

        assertThat(details.password).isEqualTo("secret-token")
        details.eraseCredentials()
        assertThat(details.password).isEmpty()
    }

    @Test
    fun `toPreAuthenticatedAuthenticationToken has correct structure`() {
        val details = CommonFixtures.userDetails()
        val token = details.toPreAuthenticatedAuthenticationToken()

        assertThat(token).isInstanceOf(PreAuthenticatedAuthenticationToken::class.java)
        assertThat(token.principal).isSameAs(details)
        assertThat(token.credentials).isNull()
        assertThat(token.authorities.map { it.authority })
            .containsExactlyInAnyOrderElementsOf(details.authorities.map { it.authority })
    }

    @Test
    fun `toString hides token value`() {
        val details = CommonFixtures.userDetails(token = "super-secret")
        val str = details.toString()

        assertThat(str).doesNotContain("super-secret")
        assertThat(str).contains(CommonFixtures.USER_ID.toString())
        assertThat(str).contains(CommonFixtures.USER_NAME)
    }
}
