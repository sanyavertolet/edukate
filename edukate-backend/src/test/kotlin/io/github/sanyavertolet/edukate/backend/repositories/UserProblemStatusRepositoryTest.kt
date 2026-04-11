package io.github.sanyavertolet.edukate.backend.repositories

import io.github.sanyavertolet.edukate.backend.BackendFixtures
import io.github.sanyavertolet.edukate.backend.MongoTestContainerConfig
import io.github.sanyavertolet.edukate.backend.configs.MongoConfig
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.mongodb.test.autoconfigure.DataMongoTest
import org.springframework.context.annotation.Import
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import reactor.test.StepVerifier

@DataMongoTest
@Import(MongoConfig::class, MongoTestContainerConfig::class)
class UserProblemStatusRepositoryTest {

    @Autowired private lateinit var repository: UserProblemStatusRepository

    @Autowired private lateinit var mongoTemplate: ReactiveMongoTemplate

    @BeforeEach
    fun cleanUp() {
        mongoTemplate.dropCollection("user_problem_statuses").block()
    }

    // region findByUserIdAndProblemId

    @Test
    fun `findByUserIdAndProblemId returns record when it exists`() {
        mongoTemplate.save(BackendFixtures.userProblemStatus(userId = "user-1", problemId = "1.0.0")).block()

        StepVerifier.create(repository.findByUserIdAndProblemId("user-1", "1.0.0"))
            .assertNext { status ->
                assertThat(status.userId).isEqualTo("user-1")
                assertThat(status.problemId).isEqualTo("1.0.0")
            }
            .verifyComplete()
    }

    @Test
    fun `findByUserIdAndProblemId returns empty Mono when no record exists`() {
        mongoTemplate.save(BackendFixtures.userProblemStatus(userId = "user-2", problemId = "1.0.0")).block()

        StepVerifier.create(repository.findByUserIdAndProblemId("user-1", "1.0.0")).verifyComplete()
    }

    @Test
    fun `findByUserIdAndProblemId does not return record for different problemId`() {
        mongoTemplate.save(BackendFixtures.userProblemStatus(userId = "user-1", problemId = "2.0.0")).block()

        StepVerifier.create(repository.findByUserIdAndProblemId("user-1", "1.0.0")).verifyComplete()
    }

    // endregion
}
