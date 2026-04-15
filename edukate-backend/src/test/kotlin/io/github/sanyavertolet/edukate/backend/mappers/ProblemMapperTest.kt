@file:Suppress("ReactiveStreamsUnusedPublisher")

package io.github.sanyavertolet.edukate.backend.mappers

import io.github.sanyavertolet.edukate.backend.BackendFixtures
import io.github.sanyavertolet.edukate.backend.entities.Problem
import io.github.sanyavertolet.edukate.backend.services.ProblemStatusDecisionManager
import io.github.sanyavertolet.edukate.backend.services.files.FileManager
import io.github.sanyavertolet.edukate.storage.keys.ProblemFileKey
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import reactor.core.publisher.Mono
import reactor.test.StepVerifier

class ProblemMapperTest {
    private val problemStatusDecisionManager: ProblemStatusDecisionManager = mockk()
    private val fileManager: FileManager = mockk()
    private lateinit var mapper: ProblemMapper

    @BeforeEach
    fun setUp() {
        mapper = ProblemMapper(problemStatusDecisionManager, fileManager)
    }

    @Test
    fun `toDto zips status and images`() {
        val auth = BackendFixtures.mockAuthentication()
        val problem = BackendFixtures.problem(id = "1.0.0", images = listOf("img.png"))
        every { problemStatusDecisionManager.getStatus("1.0.0", auth) } returns Mono.just(Problem.Status.SOLVED)
        every { fileManager.getPresignedUrl(ProblemFileKey("1.0.0", "img.png")) } returns Mono.just("https://s3/img.png")

        StepVerifier.create(mapper.toDto(problem, auth))
            .assertNext { dto ->
                assertThat(dto.status).isEqualTo(Problem.Status.SOLVED)
                assertThat(dto.images).containsExactly("https://s3/img.png")
            }
            .verifyComplete()
    }

    @Test
    fun `toMetadata applies status`() {
        val auth = BackendFixtures.mockAuthentication()
        val problem = BackendFixtures.problem(id = "1.0.0")
        every { problemStatusDecisionManager.getStatus("1.0.0", auth) } returns Mono.just(Problem.Status.SOLVING)

        StepVerifier.create(mapper.toMetadata(problem, auth))
            .assertNext { meta -> assertThat(meta.status).isEqualTo(Problem.Status.SOLVING) }
            .verifyComplete()
    }
}
