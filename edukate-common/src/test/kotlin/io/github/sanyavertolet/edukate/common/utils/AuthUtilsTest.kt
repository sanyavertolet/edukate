package io.github.sanyavertolet.edukate.common.utils

import io.github.sanyavertolet.edukate.common.CommonFixtures
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.security.core.Authentication
import reactor.test.StepVerifier

class AuthUtilsTest {

    private val userDetails = CommonFixtures.userDetails(id = 1L)
    private val auth: Authentication = mockk { every { principal } returns userDetails }

    @Test
    fun `id extracts user id from authentication`() {
        assertThat(auth.id()).isEqualTo(1L)
        assertThat(null.id()).isNull()
    }

    @Test
    fun `monoId emits user id for valid authentication`() {
        StepVerifier.create(auth.monoId()).expectNext(1L).verifyComplete()
    }

    @Test
    fun `monoId emits nothing for null authentication`() {
        StepVerifier.create(null.monoId()).verifyComplete()
    }
}
