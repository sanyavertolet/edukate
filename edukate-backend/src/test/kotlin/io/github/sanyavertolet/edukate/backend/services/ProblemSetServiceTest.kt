@file:Suppress("ReactiveStreamsUnusedPublisher")

package io.github.sanyavertolet.edukate.backend.services

import io.github.sanyavertolet.edukate.backend.BackendFixtures
import io.github.sanyavertolet.edukate.backend.dtos.CreateProblemSetRequest
import io.github.sanyavertolet.edukate.backend.entities.ProblemSet
import io.github.sanyavertolet.edukate.backend.entities.ProblemSetProblem
import io.github.sanyavertolet.edukate.backend.permissions.ProblemSetPermissionEvaluator
import io.github.sanyavertolet.edukate.backend.repositories.ProblemRepository
import io.github.sanyavertolet.edukate.backend.repositories.ProblemSetProblemRepository
import io.github.sanyavertolet.edukate.backend.repositories.ProblemSetRepository
import io.github.sanyavertolet.edukate.common.users.UserRole
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.test.StepVerifier

class ProblemSetServiceTest {

    private val problemSetRepository: ProblemSetRepository = mockk()
    private val problemSetProblemRepository: ProblemSetProblemRepository = mockk()
    private val problemRepository: ProblemRepository = mockk()
    private val shareCodeGenerator: ShareCodeGenerator = mockk()
    private val problemSetPermissionEvaluator: ProblemSetPermissionEvaluator = mockk()
    private lateinit var service: ProblemSetService

    @BeforeEach
    fun setUp() {
        service =
            ProblemSetService(
                problemSetRepository,
                problemSetProblemRepository,
                problemRepository,
                shareCodeGenerator,
                problemSetPermissionEvaluator,
            )
    }

    // region findByShareCode

    @Test
    fun `findByShareCode returns problem set when found`() {
        val ps = BackendFixtures.problemSet(shareCode = "CODE1")
        every { problemSetRepository.findByShareCode("CODE1") } returns Mono.just(ps)

        StepVerifier.create(service.findByShareCode("CODE1")).expectNext(ps).verifyComplete()
    }

    @Test
    fun `findByShareCode emits NOT_FOUND when share code is unknown`() {
        every { problemSetRepository.findByShareCode("UNKNOWN") } returns Mono.empty()

        StepVerifier.create(service.findByShareCode("UNKNOWN"))
            .expectErrorMatches { it is ResponseStatusException && it.statusCode == HttpStatus.NOT_FOUND }
            .verify()
    }

    // endregion

    // region getPublicProblemSets

    @Test
    fun `getPublicProblemSets returns only public problem sets`() {
        val ps = BackendFixtures.problemSet(isPublic = true)
        every { problemSetRepository.findByIsPublic(true, any()) } returns Flux.just(ps)

        StepVerifier.create(service.getPublicProblemSets(mockk(relaxed = true))).expectNext(ps).verifyComplete()
    }

    // endregion

    // region createProblemSet

    @Test
    fun `createProblemSet saves problem set with authenticated user as ADMIN`() {
        val auth = BackendFixtures.mockAuthentication(userId = 1L)
        val request =
            CreateProblemSetRequest(
                name = "My Set",
                description = "Desc",
                isPublic = false,
                problemKeys = listOf("savchenko/P1"),
            )
        val p1 = BackendFixtures.problem(id = 1L, code = "P1")

        every { problemRepository.findByKeyIn(listOf("savchenko/P1")) } returns Flux.just(p1)
        every { shareCodeGenerator.generateShareCode() } returns "NEWCODE"
        every { problemSetRepository.save(any()) } answers
            {
                val ps = firstArg<ProblemSet>()
                Mono.just(ps.copy(id = 1L))
            }
        every { problemSetProblemRepository.saveAll(any<Iterable<ProblemSetProblem>>()) } returns Flux.empty()

        StepVerifier.create(service.createProblemSet(request, auth))
            .assertNext { saved ->
                assert(saved.userIdRoleMap[1L] == UserRole.ADMIN)
                assert(saved.shareCode == "NEWCODE")
            }
            .verifyComplete()
    }

    // endregion

    // region removeUser

    @Test
    fun `removeUser removes user from problem set`() {
        val ps =
            BackendFixtures.problemSet(
                userIdRoleMap = mapOf(100L to UserRole.ADMIN, 1L to UserRole.USER),
                shareCode = "REM1",
            )
        every { problemSetRepository.findByShareCode("REM1") } returns Mono.just(ps)
        every { problemSetRepository.save(any()) } answers { Mono.just(firstArg()) }

        StepVerifier.create(service.removeUser("REM1", 1L))
            .assertNext { updated -> assert(!updated.isUserInProblemSet(1L)) }
            .verifyComplete()
    }

    @Test
    fun `removeUser emits BAD_REQUEST when user is not in problem set`() {
        val ps = BackendFixtures.problemSet(userIdRoleMap = mapOf(100L to UserRole.ADMIN), shareCode = "REM2")
        every { problemSetRepository.findByShareCode("REM2") } returns Mono.just(ps)

        StepVerifier.create(service.removeUser("REM2", 999L))
            .expectErrorMatches { it is ResponseStatusException && it.statusCode == HttpStatus.BAD_REQUEST }
            .verify()
    }

    @Test
    fun `removeUser emits BAD_REQUEST when last admin tries to leave`() {
        val ps = BackendFixtures.problemSet(userIdRoleMap = mapOf(100L to UserRole.ADMIN), shareCode = "REM3")
        every { problemSetRepository.findByShareCode("REM3") } returns Mono.just(ps)

        StepVerifier.create(service.removeUser("REM3", 100L))
            .expectErrorMatches { it is ResponseStatusException && it.statusCode == HttpStatus.BAD_REQUEST }
            .verify()
    }

    // endregion

    // region inviteUser

    @Test
    fun `inviteUser adds user to invitedUserIds`() {
        val ps =
            BackendFixtures.problemSet(
                userIdRoleMap = mapOf(100L to UserRole.ADMIN),
                invitedUserIds = emptySet(),
                shareCode = "INV1",
            )
        every { problemSetRepository.findByShareCode("INV1") } returns Mono.just(ps)
        every { problemSetPermissionEvaluator.hasInvitePermission(ps, 100L) } returns true
        every { problemSetRepository.save(any()) } answers { Mono.just(firstArg()) }

        StepVerifier.create(service.inviteUser("INV1", 100L, 300L))
            .assertNext { updated -> assert(updated.isUserInvited(300L)) }
            .verifyComplete()
    }

    @Test
    fun `inviteUser emits FORBIDDEN when requester lacks invite permission`() {
        val ps =
            BackendFixtures.problemSet(
                userIdRoleMap = mapOf(100L to UserRole.ADMIN, 1L to UserRole.USER),
                shareCode = "INV2",
            )
        every { problemSetRepository.findByShareCode("INV2") } returns Mono.just(ps)
        every { problemSetPermissionEvaluator.hasInvitePermission(ps, 1L) } returns false

        StepVerifier.create(service.inviteUser("INV2", 1L, 300L))
            .expectErrorMatches { it is ResponseStatusException && it.statusCode == HttpStatus.FORBIDDEN }
            .verify()
    }

    @Test
    fun `inviteUser emits BAD_REQUEST when invitee is already in problem set`() {
        val ps =
            BackendFixtures.problemSet(
                userIdRoleMap = mapOf(100L to UserRole.ADMIN, 2L to UserRole.USER),
                shareCode = "INV3",
            )
        every { problemSetRepository.findByShareCode("INV3") } returns Mono.just(ps)
        every { problemSetPermissionEvaluator.hasInvitePermission(ps, 100L) } returns true

        StepVerifier.create(service.inviteUser("INV3", 100L, 2L))
            .expectErrorMatches { it is ResponseStatusException && it.statusCode == HttpStatus.BAD_REQUEST }
            .verify()
    }

    // endregion

    // region changeVisibility

    @Test
    fun `changeVisibility sets isPublic when requester has MODERATOR role`() {
        val auth = BackendFixtures.mockAuthentication(userId = 2L)
        val ps = BackendFixtures.problemSet(isPublic = false, shareCode = "VIS1")
        every { problemSetRepository.findByShareCode("VIS1") } returns Mono.just(ps)
        every { problemSetPermissionEvaluator.hasRole(ps, UserRole.MODERATOR, auth) } returns true
        every { problemSetRepository.save(any()) } answers { Mono.just(firstArg()) }

        StepVerifier.create(service.changeVisibility("VIS1", true, auth))
            .assertNext { updated -> assert(updated.isPublic) }
            .verifyComplete()
    }

    @Test
    fun `changeVisibility emits FORBIDDEN when requester lacks MODERATOR role`() {
        val auth = BackendFixtures.mockAuthentication(userId = 3L)
        val ps = BackendFixtures.problemSet(shareCode = "VIS2")
        every { problemSetRepository.findByShareCode("VIS2") } returns Mono.just(ps)
        every { problemSetPermissionEvaluator.hasRole(ps, UserRole.MODERATOR, auth) } returns false

        StepVerifier.create(service.changeVisibility("VIS2", true, auth))
            .expectErrorMatches { it is ResponseStatusException && it.statusCode == HttpStatus.FORBIDDEN }
            .verify()
    }

    // endregion

    // region changeProblems

    @Test
    fun `changeProblems updates problemIds when authorized`() {
        val auth = BackendFixtures.mockAuthentication()
        val ps = BackendFixtures.problemSet(id = 10L, shareCode = "PROB2")
        val p1 = BackendFixtures.problem(id = 1L, code = "P1")
        val p2 = BackendFixtures.problem(id = 2L, code = "P2")
        every { problemSetRepository.findByShareCode("PROB2") } returns Mono.just(ps)
        every { problemSetPermissionEvaluator.hasRole(ps, UserRole.MODERATOR, auth) } returns true
        every { problemRepository.findByKeyIn(listOf("savchenko/P1", "savchenko/P2")) } returns Flux.just(p1, p2)
        every { problemSetProblemRepository.deleteByProblemSetId(10L) } returns Mono.empty()
        every { problemSetProblemRepository.saveAll(any<Iterable<ProblemSetProblem>>()) } returns Flux.empty()

        StepVerifier.create(service.changeProblems("PROB2", listOf("savchenko/P1", "savchenko/P2"), auth))
            .assertNext { updated -> assert(updated.id == 10L) }
            .verifyComplete()
    }

    // endregion
}
