package io.github.sanyavertolet.edukate.common

import io.github.sanyavertolet.edukate.common.checks.CheckStatus
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class SubmissionStatusTest {

    @Test
    fun `from maps CheckStatus to correct SubmissionStatus`() {
        assertThat(SubmissionStatus.from(CheckStatus.SUCCESS)).isEqualTo(SubmissionStatus.SUCCESS)
        assertThat(SubmissionStatus.from(CheckStatus.MISTAKE)).isEqualTo(SubmissionStatus.FAILED)
        assertThat(SubmissionStatus.from(CheckStatus.INTERNAL_ERROR)).isEqualTo(SubmissionStatus.FAILED)
    }

    @Test
    fun `best returns the higher-ranked status`() {
        assertThat(SubmissionStatus.best(SubmissionStatus.PENDING, SubmissionStatus.PENDING))
            .isEqualTo(SubmissionStatus.PENDING)
        assertThat(SubmissionStatus.best(SubmissionStatus.PENDING, SubmissionStatus.FAILED))
            .isEqualTo(SubmissionStatus.FAILED)
        assertThat(SubmissionStatus.best(SubmissionStatus.FAILED, SubmissionStatus.PENDING))
            .isEqualTo(SubmissionStatus.FAILED)
        assertThat(SubmissionStatus.best(SubmissionStatus.PENDING, SubmissionStatus.SUCCESS))
            .isEqualTo(SubmissionStatus.SUCCESS)
        assertThat(SubmissionStatus.best(SubmissionStatus.SUCCESS, SubmissionStatus.PENDING))
            .isEqualTo(SubmissionStatus.SUCCESS)
        assertThat(SubmissionStatus.best(SubmissionStatus.FAILED, SubmissionStatus.FAILED))
            .isEqualTo(SubmissionStatus.FAILED)
        assertThat(SubmissionStatus.best(SubmissionStatus.FAILED, SubmissionStatus.SUCCESS))
            .isEqualTo(SubmissionStatus.SUCCESS)
        assertThat(SubmissionStatus.best(SubmissionStatus.SUCCESS, SubmissionStatus.FAILED))
            .isEqualTo(SubmissionStatus.SUCCESS)
        assertThat(SubmissionStatus.best(SubmissionStatus.SUCCESS, SubmissionStatus.SUCCESS))
            .isEqualTo(SubmissionStatus.SUCCESS)
    }
}
