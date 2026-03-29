package io.github.sanyavertolet.edukate.checker.domain

import io.github.sanyavertolet.edukate.checker.CheckerFixtures
import org.assertj.core.api.Assertions.assertThatCode
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class RequestContextTest {
    private val media = listOf(CheckerFixtures.mockMedia())

    @Test
    fun `valid context is created`() {
        assertThatCode {
            RequestContext("Solve x^2 = 4", media, media)
        }.doesNotThrowAnyException()
    }

    @Test
    fun `blank problem text throws`() {
        assertThatThrownBy {
            RequestContext("", media, media)
        }.isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun `whitespace-only problem text throws`() {
        assertThatThrownBy {
            RequestContext("   ", media, media)
        }.isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun `empty submission images throws`() {
        assertThatThrownBy {
            RequestContext("Solve x^2 = 4", media, emptyList())
        }.isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun `empty problem images is allowed`() {
        assertThatCode {
            RequestContext("Solve x^2 = 4", emptyList(), media)
        }.doesNotThrowAnyException()
    }
}
