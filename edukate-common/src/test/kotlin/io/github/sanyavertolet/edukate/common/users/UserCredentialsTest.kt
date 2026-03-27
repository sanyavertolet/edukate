package io.github.sanyavertolet.edukate.common.users

import io.github.sanyavertolet.edukate.common.CommonFixtures
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class UserCredentialsTest {

    @Test
    fun `newUser sets expected defaults`() {
        val creds = UserCredentials.newUser(CommonFixtures.USER_NAME, CommonFixtures.ENCODED_PASSWORD, CommonFixtures.EMAIL)

        assertThat(creds.id).isNull()
        assertThat(creds.username).isEqualTo(CommonFixtures.USER_NAME)
        assertThat(creds.encodedPassword).isEqualTo(CommonFixtures.ENCODED_PASSWORD)
        assertThat(creds.email).isEqualTo(CommonFixtures.EMAIL)
        assertThat(creds.roles).containsExactly(UserRole.USER)
        assertThat(creds.status).isEqualTo(UserStatus.PENDING)
    }

    @Test
    fun `toString hides encodedPassword`() {
        val creds = CommonFixtures.userCredentials()
        val str = creds.toString()

        assertThat(str).doesNotContain(CommonFixtures.ENCODED_PASSWORD)
        assertThat(str).doesNotContain("encodedPassword")
        assertThat(str).contains(CommonFixtures.USER_NAME)
        assertThat(str).contains(CommonFixtures.EMAIL)
    }
}
