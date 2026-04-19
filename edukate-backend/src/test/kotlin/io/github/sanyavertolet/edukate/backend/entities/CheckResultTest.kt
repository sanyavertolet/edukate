package io.github.sanyavertolet.edukate.backend.entities

import io.github.sanyavertolet.edukate.backend.BackendFixtures
import io.github.sanyavertolet.edukate.common.checks.CheckErrorType
import io.github.sanyavertolet.edukate.common.checks.CheckStatus
import java.time.Instant
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class CheckResultTest {

    // region CheckResult.self()

    @Test
    fun `self() creates a SUCCESS check result with low trust level`() {
        val result = CheckResult.self(42L)
        assertThat(result.submissionId).isEqualTo(42L)
        assertThat(result.status).isEqualTo(CheckStatus.SUCCESS)
        assertThat(result.trustLevel).isEqualTo(0.01f)
        assertThat(result.errorType).isEqualTo(CheckErrorType.NONE)
        assertThat(result.id).isNull()
        assertThat(result.createdAt).isNull()
    }

    // endregion

    // region CheckResult.fromCheckResultMessage()

    @Test
    fun `fromCheckResultMessage maps all fields correctly`() {
        val message =
            BackendFixtures.checkResultMessage(
                submissionId = 99L,
                status = CheckStatus.MISTAKE,
                trustLevel = 0.75f,
                errorType = CheckErrorType.CONCEPTUAL,
                explanation = "Conceptual error in solution",
            )
        val result = CheckResult.fromCheckResultMessage(message)
        assertThat(result.submissionId).isEqualTo(99L)
        assertThat(result.status).isEqualTo(CheckStatus.MISTAKE)
        assertThat(result.trustLevel).isEqualTo(0.75f)
        assertThat(result.errorType).isEqualTo(CheckErrorType.CONCEPTUAL)
        assertThat(result.explanation).isEqualTo("Conceptual error in solution")
        assertThat(result.id).isNull()
        assertThat(result.createdAt).isNull()
    }

    // endregion

    // region toCheckResultDto()

    @Test
    fun `toCheckResultDto maps fields to DTO`() {
        val now = Instant.now()
        val result =
            BackendFixtures.checkResult(
                status = CheckStatus.SUCCESS,
                trustLevel = 0.9f,
                errorType = CheckErrorType.NONE,
                explanation = "All good",
                createdAt = now,
            )
        val dto = result.toCheckResultDto()
        assertThat(dto.status).isEqualTo(CheckStatus.SUCCESS)
        assertThat(dto.trustLevel).isEqualTo(0.9f)
        assertThat(dto.errorType).isEqualTo(CheckErrorType.NONE)
        assertThat(dto.explanation).isEqualTo("All good")
        assertThat(dto.createdAt).isEqualTo(now)
    }

    @Test
    fun `toCheckResultDto throws when createdAt is null`() {
        val result = BackendFixtures.checkResult(createdAt = null)
        assertThatThrownBy { result.toCheckResultDto() }.isInstanceOf(IllegalArgumentException::class.java)
    }

    // endregion

    // region toCheckResultInfo()

    @Test
    fun `toCheckResultInfo maps id, status, trustLevel, and createdAt`() {
        val now = Instant.now()
        val result = BackendFixtures.checkResult(id = 1L, status = CheckStatus.SUCCESS, trustLevel = 0.8f, createdAt = now)
        val info = result.toCheckResultInfo()
        assertThat(info.id).isEqualTo(1L)
        assertThat(info.status).isEqualTo(CheckStatus.SUCCESS)
        assertThat(info.trustLevel).isEqualTo(0.8f)
        assertThat(info.createdAt).isEqualTo(now)
    }

    @Test
    fun `toCheckResultInfo throws when id is null`() {
        val result = BackendFixtures.checkResult(id = null, createdAt = Instant.now())
        assertThatThrownBy { result.toCheckResultInfo() }.isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun `toCheckResultInfo throws when createdAt is null`() {
        val result = BackendFixtures.checkResult(id = 1L, createdAt = null)
        assertThatThrownBy { result.toCheckResultInfo() }.isInstanceOf(IllegalArgumentException::class.java)
    }

    // endregion
}
