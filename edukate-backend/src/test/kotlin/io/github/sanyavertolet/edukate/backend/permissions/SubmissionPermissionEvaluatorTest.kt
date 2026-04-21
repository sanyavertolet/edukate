package io.github.sanyavertolet.edukate.backend.permissions

import io.github.sanyavertolet.edukate.backend.BackendFixtures
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class SubmissionPermissionEvaluatorTest {

    private lateinit var evaluator: SubmissionPermissionEvaluator

    @BeforeEach
    fun setUp() {
        evaluator = SubmissionPermissionEvaluator()
    }

    @Test
    fun `isOwner returns true when userId matches submission owner`() {
        val submission = BackendFixtures.submission(userId = 1L)
        assertThat(evaluator.isOwner(submission, 1L)).isTrue()
    }

    @Test
    fun `isOwner returns false when userId does not match submission owner`() {
        val submission = BackendFixtures.submission(userId = 1L)
        assertThat(evaluator.isOwner(submission, 2L)).isFalse()
    }
}
