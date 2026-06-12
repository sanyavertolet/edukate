@file:Suppress("ReactiveStreamsUnusedPublisher")

package io.github.sanyavertolet.edukate.backend.mappers

import io.github.sanyavertolet.edukate.backend.BackendFixtures
import io.github.sanyavertolet.edukate.backend.dtos.ProblemMetadata
import io.github.sanyavertolet.edukate.backend.entities.Problem
import io.github.sanyavertolet.edukate.backend.entities.ProblemSetProblem
import io.github.sanyavertolet.edukate.backend.repositories.ProblemProgressRepository
import io.github.sanyavertolet.edukate.backend.repositories.ProblemSetProblemRepository
import io.github.sanyavertolet.edukate.backend.services.ProblemService
import io.github.sanyavertolet.edukate.backend.services.UserService
import io.github.sanyavertolet.edukate.common.ContentLanguage
import io.github.sanyavertolet.edukate.common.users.UserRole
import io.mockk.every
import io.mockk.mockk
import java.time.Instant
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.test.StepVerifier

class ProblemSetMapperTest {
    private val problemService: ProblemService = mockk()
    private val problemMapper: ProblemMapper = mockk()
    private val userService: UserService = mockk()
    private val problemSetProblemRepository: ProblemSetProblemRepository = mockk()
    private val problemProgressRepository: ProblemProgressRepository = mockk()
    private lateinit var mapper: ProblemSetMapper

    @BeforeEach
    fun setUp() {
        mapper =
            ProblemSetMapper(
                problemService,
                problemMapper,
                userService,
                problemSetProblemRepository,
                problemProgressRepository,
            )
    }

    private fun metadata(code: String): ProblemMetadata =
        ProblemMetadata(
            key = "savchenko/$code",
            code = code,
            bookSlug = "savchenko",
            isHard = false,
            tags = emptyList(),
            status = Problem.Status.NOT_SOLVED,
            createdAt = Instant.parse("2026-01-01T10:00:00Z"),
            language = ContentLanguage.RU,
        )

    @Test
    fun `toDto returns problems in saved position order even when findProblemsByIds returns them out of order`() {
        val ps = BackendFixtures.problemSet(id = 10L, userIdRoleMap = mapOf(100L to UserRole.ADMIN))
        // Join-table order is [P3, P1, P2] at positions 0, 1, 2.
        every { problemSetProblemRepository.findByProblemSetIdOrderByPosition(10L) } returns
            Flux.just(ProblemSetProblem(10L, 3L, 0), ProblemSetProblem(10L, 1L, 1), ProblemSetProblem(10L, 2L, 2))
        val p1 = BackendFixtures.problem(id = 1L, code = "P1")
        val p2 = BackendFixtures.problem(id = 2L, code = "P2")
        val p3 = BackendFixtures.problem(id = 3L, code = "P3")
        // DB returns problems in id-ascending order — this is what PostgreSQL typically does for
        // IN(...).
        // The mapper must reorder them back to match the position-ordered ids it just queried.
        every { problemService.findProblemsByIds(listOf(3L, 1L, 2L)) } returns Flux.just(p1, p2, p3)
        every { problemMapper.toMetadata(p1, null) } returns Mono.just(metadata("P1"))
        every { problemMapper.toMetadata(p2, null) } returns Mono.just(metadata("P2"))
        every { problemMapper.toMetadata(p3, null) } returns Mono.just(metadata("P3"))
        every { userService.findUserById(100L) } returns Mono.just(BackendFixtures.user(id = 100L, name = "alice"))

        StepVerifier.create(mapper.toDto(ps, null))
            .assertNext { dto ->
                val codes = dto.problems.map { it.code }
                assert(codes == listOf("P3", "P1", "P2")) {
                    "Expected [P3, P1, P2] (the saved position order) but got $codes. " +
                        "The mapper is returning problems in DB order instead of position order."
                }
            }
            .verifyComplete()
    }
}
