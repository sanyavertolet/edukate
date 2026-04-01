package io.github.sanyavertolet.edukate.backend.utils

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class SemVerUtilsTest {
    @Test
    fun `parseValidVersion returns correct triple`() {
        assertThat(SemVerUtils.parse("1.2.3")).isEqualTo(Triple(1, 2, 3))
    }

    @Test
    fun `parseZeroVersion returns zero triple`() {
        assertThat(SemVerUtils.parse("0.0.0")).isEqualTo(Triple(0, 0, 0))
    }

    @Test
    fun `parseLargeNumbers returns correct values`() {
        val (major, minor, patch) = SemVerUtils.parse("100.200.300")
        assertThat(major).isEqualTo(100)
        assertThat(minor).isEqualTo(200)
        assertThat(patch).isEqualTo(300)
    }

    @Test
    fun `parseSingleSegmentThrows`() {
        assertThatThrownBy { SemVerUtils.parse("1") }.isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun `parseTwoSegmentsThrows`() {
        assertThatThrownBy { SemVerUtils.parse("1.2") }.isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun `parseFourSegmentsThrows`() {
        assertThatThrownBy { SemVerUtils.parse("1.2.3.4") }.isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun `parseNonNumericThrows`() {
        assertThatThrownBy { SemVerUtils.parse("a.b.c") }.isInstanceOf(NumberFormatException::class.java)
    }
}
