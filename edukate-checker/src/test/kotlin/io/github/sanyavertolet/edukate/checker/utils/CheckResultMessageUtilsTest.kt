package io.github.sanyavertolet.edukate.checker.utils

import io.github.sanyavertolet.edukate.checker.CheckerFixtures
import io.github.sanyavertolet.edukate.checker.dtos.ModelResponse
import io.github.sanyavertolet.edukate.common.checks.CheckErrorType
import io.github.sanyavertolet.edukate.common.checks.CheckStatus
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class CheckResultMessageUtilsTest {
    private val ctx = CheckerFixtures.submissionContext()

    @Test
    fun `success maps status and trust level`() {
        val response = ModelResponse(CheckStatus.MISTAKE, 0.7f, CheckErrorType.ALGEBRAIC, "Wrong.")
        val result = success(response, ctx)
        assertThat(result.status).isEqualTo(CheckStatus.MISTAKE)
        assertThat(result.trustLevel).isEqualTo(0.7f)
        assertThat(result.errorType).isEqualTo(CheckErrorType.ALGEBRAIC)
    }

    @Test
    fun `success clamps trustLevel greater than 1 to 1`() {
        val response = ModelResponse(CheckStatus.SUCCESS, 1.5f, CheckErrorType.NONE, "")
        val result = success(response, ctx)
        assertThat(result.trustLevel).isEqualTo(1.0f)
    }

    @Test
    fun `success clamps trustLevel less than 0 to 0`() {
        val response = ModelResponse(CheckStatus.SUCCESS, -0.3f, CheckErrorType.NONE, "")
        val result = success(response, ctx)
        assertThat(result.trustLevel).isEqualTo(0.0f)
    }

    @Test
    fun `success forces errorType NONE when status is SUCCESS`() {
        val response = ModelResponse(CheckStatus.SUCCESS, 0.9f, CheckErrorType.ALGEBRAIC, "")
        val result = success(response, ctx)
        assertThat(result.errorType).isEqualTo(CheckErrorType.NONE)
    }

    @Test
    fun `success preserves errorType when status is MISTAKE`() {
        val response = ModelResponse(CheckStatus.MISTAKE, 0.5f, CheckErrorType.CONCEPTUAL, "")
        val result = success(response, ctx)
        assertThat(result.errorType).isEqualTo(CheckErrorType.CONCEPTUAL)
    }

    @Test
    fun `success carries submissionId from context`() {
        val response = ModelResponse(CheckStatus.SUCCESS, 0.9f, CheckErrorType.NONE, "")
        val result = success(response, ctx)
        assertThat(result.submissionId).isEqualTo(ctx.submissionId)
    }

    @Test
    fun `error returns INTERNAL_ERROR`() {
        val result = error(ctx)
        assertThat(result.status).isEqualTo(CheckStatus.INTERNAL_ERROR)
    }

    @Test
    fun `error sets trustLevel to 0`() {
        val result = error(ctx)
        assertThat(result.trustLevel).isEqualTo(0f)
    }

    @Test
    fun `error explanation is non-blank`() {
        val result = error(ctx)
        assertThat(result.explanation).isNotBlank()
    }

    @Test
    fun `error carries submissionId from context`() {
        val result = error(ctx)
        assertThat(result.submissionId).isEqualTo(ctx.submissionId)
    }
}
