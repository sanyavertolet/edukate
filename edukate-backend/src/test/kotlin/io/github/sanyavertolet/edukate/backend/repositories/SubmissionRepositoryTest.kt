package io.github.sanyavertolet.edukate.backend.repositories

import io.github.sanyavertolet.edukate.backend.BackendFixtures
import io.github.sanyavertolet.edukate.backend.configs.MongoConfig
import io.github.sanyavertolet.edukate.common.SubmissionStatus
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest
import org.springframework.context.annotation.Import
import org.springframework.data.domain.PageRequest
import reactor.test.StepVerifier

@DataMongoTest
@Import(MongoConfig::class)
class SubmissionRepositoryTest {

    @Autowired private lateinit var repository: SubmissionRepository

    @BeforeEach
    fun cleanUp() {
        repository.deleteAll().block()
    }

    // region findAllByProblemIdAndUserId

    @Test
    fun `findAllByProblemIdAndUserId returns submissions matching both problemId and userId`() {
        repository.save(BackendFixtures.submission(id = null, problemId = "1.0.0", userId = "user-1")).block()
        repository.save(BackendFixtures.submission(id = null, problemId = "1.0.0", userId = "user-2")).block()
        repository.save(BackendFixtures.submission(id = null, problemId = "2.0.0", userId = "user-1")).block()

        val pageable = PageRequest.of(0, 10)

        StepVerifier.create(repository.findAllByProblemIdAndUserId("1.0.0", "user-1", pageable))
            .assertNext { sub ->
                assertThat(sub.problemId).isEqualTo("1.0.0")
                assertThat(sub.userId).isEqualTo("user-1")
            }
            .verifyComplete()
    }

    @Test
    fun `findAllByProblemIdAndUserId returns empty flux when no match`() {
        repository.save(BackendFixtures.submission(id = null, problemId = "1.0.0", userId = "user-2")).block()

        val pageable = PageRequest.of(0, 10)

        StepVerifier.create(repository.findAllByProblemIdAndUserId("1.0.0", "user-1", pageable)).verifyComplete()
    }

    @Test
    fun `findAllByProblemIdAndUserId respects page size`() {
        repeat(5) { i ->
            repository.save(BackendFixtures.submission(id = null, problemId = "1.0.0", userId = "user-1")).block()
        }

        val pageable = PageRequest.of(0, 3)

        StepVerifier.create(repository.findAllByProblemIdAndUserId("1.0.0", "user-1", pageable))
            .expectNextCount(3)
            .verifyComplete()
    }

    // endregion

    // region findAllByUserId

    @Test
    fun `findAllByUserId returns all submissions for a given user`() {
        repository.save(BackendFixtures.submission(id = null, problemId = "1.0.0", userId = "user-1")).block()
        repository.save(BackendFixtures.submission(id = null, problemId = "2.0.0", userId = "user-1")).block()
        repository.save(BackendFixtures.submission(id = null, problemId = "1.0.0", userId = "user-2")).block()

        val pageable = PageRequest.of(0, 10)

        StepVerifier.create(repository.findAllByUserId("user-1", pageable))
            .expectNextCount(2)
            .verifyComplete()
    }

    @Test
    fun `findAllByUserId returns empty flux when user has no submissions`() {
        repository.save(BackendFixtures.submission(id = null, problemId = "1.0.0", userId = "user-2")).block()

        val pageable = PageRequest.of(0, 10)

        StepVerifier.create(repository.findAllByUserId("user-1", pageable)).verifyComplete()
    }

    // endregion

    // region findAllByStatusIn

    @Test
    fun `findAllByStatusIn returns submissions matching any of the given statuses`() {
        repository.save(BackendFixtures.submission(id = null, status = SubmissionStatus.PENDING)).block()
        repository.save(BackendFixtures.submission(id = null, status = SubmissionStatus.SUCCESS)).block()
        repository.save(BackendFixtures.submission(id = null, status = SubmissionStatus.FAILED)).block()

        val pageable = PageRequest.of(0, 10)

        StepVerifier.create(repository.findAllByStatusIn(listOf(SubmissionStatus.SUCCESS, SubmissionStatus.FAILED), pageable))
            .expectNextCount(2)
            .verifyComplete()
    }

    @Test
    fun `findAllByStatusIn returns empty flux when no submissions match`() {
        repository.save(BackendFixtures.submission(id = null, status = SubmissionStatus.PENDING)).block()

        val pageable = PageRequest.of(0, 10)

        StepVerifier.create(repository.findAllByStatusIn(listOf(SubmissionStatus.SUCCESS), pageable)).verifyComplete()
    }

    // endregion
}
