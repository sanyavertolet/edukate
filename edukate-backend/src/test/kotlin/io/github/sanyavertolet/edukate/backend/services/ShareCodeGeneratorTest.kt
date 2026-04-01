package io.github.sanyavertolet.edukate.backend.services

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ShareCodeGeneratorTest {
    private val generator = ShareCodeGenerator()

    @Test
    fun `generated code has length 10`() {
        assertThat(generator.generateShareCode()).hasSize(10)
    }

    @Test
    fun `generated code is alphanumeric`() {
        val code = generator.generateShareCode()
        assertThat(code).matches("[A-Za-z0-9]+")
        assertThat(code).hasSize(10)
    }

    @Test
    fun `generated codes are unique`() {
        // Probabilistic: chance of collision is 1/62^10 ≈ negligible
        val code1 = generator.generateShareCode()
        val code2 = generator.generateShareCode()
        assertThat(code1).isNotEqualTo(code2)
    }
}
