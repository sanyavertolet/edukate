package io.github.sanyavertolet.edukate.common.utils

import io.github.sanyavertolet.edukate.common.CommonFixtures
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.security.core.Authentication
import reactor.test.StepVerifier

class AuthUtilsTest {

    private val userDetails = CommonFixtures.userDetails(id = "user-1")
    private val auth: Authentication = mockk { every { principal } returns userDetails }

    @Test
    fun `id extracts user id from authentication`() {
        assertThat(AuthUtils.id(auth)).isEqualTo("user-1")
        assertThat(AuthUtils.id(null)).isNull()
    }

    @Test
    fun `monoId emits user id for valid authentication`() {
        StepVerifier.create(AuthUtils.monoId(auth)).expectNext("user-1").verifyComplete()
    }

    @Test
    fun `monoId emits nothing for null authentication`() {
        StepVerifier.create(AuthUtils.monoId(null)).verifyComplete()
    }
}
