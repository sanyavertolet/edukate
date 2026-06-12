@file:Suppress("ReactiveStreamsUnusedPublisher")

package io.github.sanyavertolet.edukate.backend.services

import io.github.sanyavertolet.edukate.backend.BackendFixtures
import io.github.sanyavertolet.edukate.backend.dtos.CreateProblemSetRequest
import io.github.sanyavertolet.edukate.backend.dtos.UpdateProblemSetSettingsRequest
import io.github.sanyavertolet.edukate.backend.entities.ProblemSet
import io.github.sanyavertolet.edukate.backend.entities.ProblemSetProblem
import io.github.sanyavertolet.edukate.backend.permissions.ProblemSetPermissionEvaluator
import io.github.sanyavertolet.edukate.backend.repositories.ProblemRepository
import io.github.sanyavertolet.edukate.backend.repositories.ProblemSetProblemRepository
import io.github.sanyavertolet.edukate.backend.repositories.ProblemSetRepository
import io.github.sanyavertolet.edukate.common.notifications.InviteNotificationCreateRequest
import io.github.sanyavertolet.edukate.common.services.Notifier
import io.github.sanyavertolet.edukate.common.users.UserRole
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
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
    private val notifier: Notifier = mockk()
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
                notifier,
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

        StepVerifier.create(service.findByShareCode("UNKNOWN")).expectStatus(HttpStatus.NOT_FOUND).verify()
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

    @Test
    fun `createProblemSet collapses blank description to null`() {
        val auth = BackendFixtures.mockAuthentication(userId = 1L)
        val request =
            CreateProblemSetRequest(
                name = "My Set",
                description = "   ",
                isPublic = false,
                problemKeys = listOf("savchenko/P1"),
            )
        val savedSlot = slot<ProblemSet>()
        every { problemRepository.findByKeyIn(any()) } returns Flux.just(BackendFixtures.problem(id = 1L, code = "P1"))
        every { shareCodeGenerator.generateShareCode() } returns "NEWCODE"
        every { problemSetRepository.save(capture(savedSlot)) } answers { Mono.just(firstArg<ProblemSet>().copy(id = 1L)) }
        every { problemSetProblemRepository.saveAll(any<Iterable<ProblemSetProblem>>()) } returns Flux.empty()

        StepVerifier.create(service.createProblemSet(request, auth)).expectNextCount(1).verifyComplete()

        assert(savedSlot.captured.description == null)
    }

    // endregion

    // region updateSettings

    @Test
    fun `updateSettings applies only the provided fields`() {
        val auth = BackendFixtures.mockAuthentication(userId = 2L)
        val ps = BackendFixtures.problemSet(name = "Old", description = "OldDesc", isPublic = false, shareCode = "U1")
        every { problemSetRepository.findByShareCode("U1") } returns Mono.just(ps)
        every { problemSetPermissionEvaluator.hasRole(ps, 2L, UserRole.MODERATOR) } returns true
        every { problemSetRepository.save(any()) } answers { Mono.just(firstArg()) }

        val request = UpdateProblemSetSettingsRequest(name = "New", isPublic = true)
        StepVerifier.create(service.updateSettings("U1", request, auth))
            .assertNext { updated ->
                assert(updated.name == "New")
                assert(updated.description == "OldDesc")
                assert(updated.isPublic)
            }
            .verifyComplete()
    }

    @Test
    fun `updateSettings skips save when nothing changes`() {
        val auth = BackendFixtures.mockAuthentication(userId = 2L)
        val ps = BackendFixtures.problemSet(shareCode = "U2")
        every { problemSetRepository.findByShareCode("U2") } returns Mono.just(ps)
        every { problemSetPermissionEvaluator.hasRole(ps, 2L, UserRole.MODERATOR) } returns true

        // No save mock set up — would throw if invoked.
        StepVerifier.create(service.updateSettings("U2", UpdateProblemSetSettingsRequest(), auth))
            .expectNext(ps)
            .verifyComplete()
    }

    @Test
    fun `updateSettings emits FORBIDDEN when caller lacks MODERATOR role`() {
        val auth = BackendFixtures.mockAuthentication(userId = 3L)
        val ps = BackendFixtures.problemSet(shareCode = "U3")
        every { problemSetRepository.findByShareCode("U3") } returns Mono.just(ps)
        every { problemSetPermissionEvaluator.hasRole(ps, 3L, UserRole.MODERATOR) } returns false

        StepVerifier.create(service.updateSettings("U3", UpdateProblemSetSettingsRequest(isPublic = true), auth))
            .expectStatus(HttpStatus.FORBIDDEN)
            .verify()
    }

    @Test
    fun `updateSettings persists problemKeys in input order even when DB returns problems out of order`() {
        val auth = BackendFixtures.mockAuthentication(userId = 2L)
        val ps = BackendFixtures.problemSet(id = 10L, shareCode = "ORD1")
        val p1 = BackendFixtures.problem(id = 1L, code = "P1")
        val p2 = BackendFixtures.problem(id = 2L, code = "P2")
        val p3 = BackendFixtures.problem(id = 3L, code = "P3")
        every { problemSetRepository.findByShareCode("ORD1") } returns Mono.just(ps)
        every { problemSetPermissionEvaluator.hasRole(ps, 2L, UserRole.MODERATOR) } returns true
        // DB returns problems in id-ascending order (typical PostgreSQL behavior with IN(...)).
        every { problemRepository.findByKeyIn(listOf("savchenko/P3", "savchenko/P1", "savchenko/P2")) } returns
            Flux.just(p1, p2, p3)
        every { problemSetProblemRepository.deleteByProblemSetId(10L) } returns Mono.empty()

        val savedSlot = slot<Iterable<ProblemSetProblem>>()
        every { problemSetProblemRepository.saveAll(capture(savedSlot)) } returns Flux.empty()

        val request = UpdateProblemSetSettingsRequest(problemKeys = listOf("savchenko/P3", "savchenko/P1", "savchenko/P2"))
        StepVerifier.create(service.updateSettings("ORD1", request, auth)).expectNextCount(1).verifyComplete()

        // The contract: position must match input order — P3 at 0, P1 at 1, P2 at 2 —
        // regardless of the order the DB returned the resolved problems.
        val saved = savedSlot.captured.toList()
        assert(
            saved == listOf(ProblemSetProblem(10L, 3L, 0), ProblemSetProblem(10L, 1L, 1), ProblemSetProblem(10L, 2L, 2))
        ) {
            "Expected positions [P3=0, P1=1, P2=2] but got $saved"
        }
    }

    @Test
    fun `createProblemSet persists problemKeys in input order`() {
        val auth = BackendFixtures.mockAuthentication(userId = 1L)
        val p1 = BackendFixtures.problem(id = 1L, code = "P1")
        val p2 = BackendFixtures.problem(id = 2L, code = "P2")
        val p3 = BackendFixtures.problem(id = 3L, code = "P3")
        val request =
            CreateProblemSetRequest(
                name = "Ordered",
                description = "x",
                isPublic = false,
                problemKeys = listOf("savchenko/P3", "savchenko/P1", "savchenko/P2"),
            )
        every { problemRepository.findByKeyIn(listOf("savchenko/P3", "savchenko/P1", "savchenko/P2")) } returns
            Flux.just(p1, p2, p3)
        every { shareCodeGenerator.generateShareCode() } returns "CREATED"
        every { problemSetRepository.save(any()) } answers { Mono.just(firstArg<ProblemSet>().copy(id = 99L)) }

        val savedSlot = slot<Iterable<ProblemSetProblem>>()
        every { problemSetProblemRepository.saveAll(capture(savedSlot)) } returns Flux.empty()

        StepVerifier.create(service.createProblemSet(request, auth)).expectNextCount(1).verifyComplete()

        val saved = savedSlot.captured.toList()
        assert(
            saved == listOf(ProblemSetProblem(99L, 3L, 0), ProblemSetProblem(99L, 1L, 1), ProblemSetProblem(99L, 2L, 2))
        ) {
            "Expected positions [P3=0, P1=1, P2=2] on create, but got $saved"
        }
    }

    @Test
    fun `updateSettings replaces problems when problemKeys is provided`() {
        val auth = BackendFixtures.mockAuthentication(userId = 2L)
        val ps = BackendFixtures.problemSet(id = 10L, shareCode = "U4")
        val p1 = BackendFixtures.problem(id = 1L, code = "P1")
        every { problemSetRepository.findByShareCode("U4") } returns Mono.just(ps)
        every { problemSetPermissionEvaluator.hasRole(ps, 2L, UserRole.MODERATOR) } returns true
        every { problemRepository.findByKeyIn(listOf("savchenko/P1")) } returns Flux.just(p1)
        every { problemSetProblemRepository.deleteByProblemSetId(10L) } returns Mono.empty()
        every { problemSetProblemRepository.saveAll(any<Iterable<ProblemSetProblem>>()) } returns Flux.empty()

        val request = UpdateProblemSetSettingsRequest(problemKeys = listOf("savchenko/P1"))
        StepVerifier.create(service.updateSettings("U4", request, auth)).expectNextCount(1).verifyComplete()

        verify { problemSetProblemRepository.deleteByProblemSetId(10L) }
        verify { problemSetProblemRepository.saveAll(any<Iterable<ProblemSetProblem>>()) }
    }

    // endregion

    // region setMemberRole

    @Test
    fun `setMemberRole updates role when authorized`() {
        val auth = BackendFixtures.mockAuthentication(userId = 100L)
        val ps =
            BackendFixtures.problemSet(userIdRoleMap = mapOf(100L to UserRole.ADMIN, 2L to UserRole.USER), shareCode = "R1")
        every { problemSetRepository.findByShareCode("R1") } returns Mono.just(ps)
        every { problemSetPermissionEvaluator.hasChangeRolePermission(ps, 100L, 2L, UserRole.MODERATOR) } returns true
        every { problemSetRepository.save(any()) } answers { Mono.just(firstArg()) }

        StepVerifier.create(service.setMemberRole("R1", 2L, UserRole.MODERATOR, auth))
            .assertNext { assert(it.getUserRole(2L) == UserRole.MODERATOR) }
            .verifyComplete()
    }

    @Test
    fun `setMemberRole emits NOT_FOUND when target is not in problem set`() {
        val auth = BackendFixtures.mockAuthentication(userId = 100L)
        val ps = BackendFixtures.problemSet(userIdRoleMap = mapOf(100L to UserRole.ADMIN), shareCode = "R2")
        every { problemSetRepository.findByShareCode("R2") } returns Mono.just(ps)

        StepVerifier.create(service.setMemberRole("R2", 999L, UserRole.USER, auth))
            .expectStatus(HttpStatus.NOT_FOUND)
            .verify()
    }

    @Test
    fun `setMemberRole emits FORBIDDEN when caller lacks permission`() {
        val auth = BackendFixtures.mockAuthentication(userId = 100L)
        val ps =
            BackendFixtures.problemSet(
                userIdRoleMap = mapOf(100L to UserRole.MODERATOR, 2L to UserRole.MODERATOR),
                shareCode = "R3",
            )
        every { problemSetRepository.findByShareCode("R3") } returns Mono.just(ps)
        every { problemSetPermissionEvaluator.hasChangeRolePermission(ps, 100L, 2L, UserRole.ADMIN) } returns false

        StepVerifier.create(service.setMemberRole("R3", 2L, UserRole.ADMIN, auth))
            .expectStatus(HttpStatus.FORBIDDEN)
            .verify()
    }

    @Test
    fun `setMemberRole emits BAD_REQUEST when demoting the last admin`() {
        val auth = BackendFixtures.mockAuthentication(userId = 100L)
        val ps =
            BackendFixtures.problemSet(userIdRoleMap = mapOf(100L to UserRole.ADMIN, 2L to UserRole.USER), shareCode = "R4")
        every { problemSetRepository.findByShareCode("R4") } returns Mono.just(ps)
        every { problemSetPermissionEvaluator.hasChangeRolePermission(ps, 100L, 100L, UserRole.MODERATOR) } returns true

        StepVerifier.create(service.setMemberRole("R4", 100L, UserRole.MODERATOR, auth))
            .expectStatus(HttpStatus.BAD_REQUEST)
            .verify()
    }

    // endregion

    // region removeMember

    @Test
    fun `removeMember removes user when authorized`() {
        val auth = BackendFixtures.mockAuthentication(userId = 100L)
        val ps =
            BackendFixtures.problemSet(userIdRoleMap = mapOf(100L to UserRole.ADMIN, 2L to UserRole.USER), shareCode = "RM1")
        every { problemSetRepository.findByShareCode("RM1") } returns Mono.just(ps)
        every { problemSetPermissionEvaluator.hasRemovePermission(ps, 100L, 2L) } returns true
        every { problemSetRepository.save(any()) } answers { Mono.just(firstArg()) }

        StepVerifier.create(service.removeMember("RM1", 2L, auth))
            .assertNext { assert(!it.isUserInProblemSet(2L)) }
            .verifyComplete()
    }

    @Test
    fun `removeMember emits BAD_REQUEST when removing the last admin`() {
        val auth = BackendFixtures.mockAuthentication(userId = 100L)
        val ps = BackendFixtures.problemSet(userIdRoleMap = mapOf(100L to UserRole.ADMIN), shareCode = "RM2")
        every { problemSetRepository.findByShareCode("RM2") } returns Mono.just(ps)
        every { problemSetPermissionEvaluator.hasRemovePermission(ps, 100L, 100L) } returns true

        StepVerifier.create(service.removeMember("RM2", 100L, auth)).expectStatus(HttpStatus.BAD_REQUEST).verify()
    }

    // endregion

    // region leaveProblemSet

    @Test
    fun `leaveProblemSet removes the caller`() {
        val auth = BackendFixtures.mockAuthentication(userId = 1L)
        val ps =
            BackendFixtures.problemSet(userIdRoleMap = mapOf(100L to UserRole.ADMIN, 1L to UserRole.USER), shareCode = "L1")
        every { problemSetRepository.findByShareCode("L1") } returns Mono.just(ps)
        every { problemSetRepository.save(any()) } answers { Mono.just(firstArg()) }

        StepVerifier.create(service.leaveProblemSet("L1", auth))
            .assertNext { assert(!it.isUserInProblemSet(1L)) }
            .verifyComplete()
    }

    @Test
    fun `leaveProblemSet emits BAD_REQUEST when caller is not a member`() {
        val auth = BackendFixtures.mockAuthentication(userId = 1L)
        val ps = BackendFixtures.problemSet(userIdRoleMap = mapOf(100L to UserRole.ADMIN), shareCode = "L2")
        every { problemSetRepository.findByShareCode("L2") } returns Mono.just(ps)

        StepVerifier.create(service.leaveProblemSet("L2", auth)).expectStatus(HttpStatus.BAD_REQUEST).verify()
    }

    @Test
    fun `leaveProblemSet emits BAD_REQUEST when caller is the last admin`() {
        val auth = BackendFixtures.mockAuthentication(userId = 100L)
        val ps = BackendFixtures.problemSet(userIdRoleMap = mapOf(100L to UserRole.ADMIN), shareCode = "L3")
        every { problemSetRepository.findByShareCode("L3") } returns Mono.just(ps)

        StepVerifier.create(service.leaveProblemSet("L3", auth)).expectStatus(HttpStatus.BAD_REQUEST).verify()
    }

    // endregion

    // region createInvitation

    @Test
    fun `createInvitation invites user and fires notification`() {
        val auth = BackendFixtures.mockAuthentication(userId = 100L, username = "admin")
        val ps =
            BackendFixtures.problemSet(
                userIdRoleMap = mapOf(100L to UserRole.ADMIN),
                invitedUserIds = emptySet(),
                shareCode = "INV1",
                name = "My Set",
            )
        every { problemSetRepository.findByShareCode("INV1") } returns Mono.just(ps)
        every { problemSetPermissionEvaluator.hasInvitePermission(ps, 100L) } returns true
        every { problemSetRepository.save(any()) } answers { Mono.just(firstArg()) }

        val notificationSlot = slot<InviteNotificationCreateRequest>()
        every { notifier.notify(capture(notificationSlot)) } returns Mono.just("notif-id")

        StepVerifier.create(service.createInvitation("INV1", 300L, auth))
            .assertNext { assert(it.isUserInvited(300L)) }
            .verifyComplete()

        assert(notificationSlot.captured.targetUserId == 300L)
        assert(notificationSlot.captured.inviterName == "admin")
        assert(notificationSlot.captured.problemSetName == "My Set")
        assert(notificationSlot.captured.problemSetShareCode == "INV1")
    }

    @Test
    fun `createInvitation emits FORBIDDEN when caller lacks invite permission`() {
        val auth = BackendFixtures.mockAuthentication(userId = 1L)
        val ps =
            BackendFixtures.problemSet(
                userIdRoleMap = mapOf(100L to UserRole.ADMIN, 1L to UserRole.USER),
                shareCode = "INV2",
            )
        every { problemSetRepository.findByShareCode("INV2") } returns Mono.just(ps)
        every { problemSetPermissionEvaluator.hasInvitePermission(ps, 1L) } returns false

        StepVerifier.create(service.createInvitation("INV2", 300L, auth)).expectStatus(HttpStatus.FORBIDDEN).verify()
    }

    @Test
    fun `createInvitation emits BAD_REQUEST when invitee is already a member`() {
        val auth = BackendFixtures.mockAuthentication(userId = 100L)
        val ps =
            BackendFixtures.problemSet(
                userIdRoleMap = mapOf(100L to UserRole.ADMIN, 2L to UserRole.USER),
                shareCode = "INV3",
            )
        every { problemSetRepository.findByShareCode("INV3") } returns Mono.just(ps)
        every { problemSetPermissionEvaluator.hasInvitePermission(ps, 100L) } returns true

        StepVerifier.create(service.createInvitation("INV3", 2L, auth)).expectStatus(HttpStatus.BAD_REQUEST).verify()
    }

    @Test
    fun `createInvitation emits BAD_REQUEST when invitee is already invited`() {
        val auth = BackendFixtures.mockAuthentication(userId = 100L)
        val ps =
            BackendFixtures.problemSet(
                userIdRoleMap = mapOf(100L to UserRole.ADMIN),
                invitedUserIds = setOf(300L),
                shareCode = "INV4",
            )
        every { problemSetRepository.findByShareCode("INV4") } returns Mono.just(ps)
        every { problemSetPermissionEvaluator.hasInvitePermission(ps, 100L) } returns true

        StepVerifier.create(service.createInvitation("INV4", 300L, auth)).expectStatus(HttpStatus.BAD_REQUEST).verify()
    }

    // endregion

    // region revokeInvitation

    @Test
    fun `revokeInvitation removes the pending invite`() {
        val auth = BackendFixtures.mockAuthentication(userId = 100L)
        val ps =
            BackendFixtures.problemSet(
                userIdRoleMap = mapOf(100L to UserRole.ADMIN),
                invitedUserIds = setOf(300L),
                shareCode = "REV1",
            )
        every { problemSetRepository.findByShareCode("REV1") } returns Mono.just(ps)
        every { problemSetPermissionEvaluator.hasInvitePermission(ps, 100L) } returns true
        every { problemSetRepository.save(any()) } answers { Mono.just(firstArg()) }

        StepVerifier.create(service.revokeInvitation("REV1", 300L, auth))
            .assertNext { assert(!it.isUserInvited(300L)) }
            .verifyComplete()
    }

    @Test
    fun `revokeInvitation emits BAD_REQUEST when user is not invited`() {
        val auth = BackendFixtures.mockAuthentication(userId = 100L)
        val ps =
            BackendFixtures.problemSet(
                userIdRoleMap = mapOf(100L to UserRole.ADMIN),
                invitedUserIds = emptySet(),
                shareCode = "REV2",
            )
        every { problemSetRepository.findByShareCode("REV2") } returns Mono.just(ps)
        every { problemSetPermissionEvaluator.hasInvitePermission(ps, 100L) } returns true

        StepVerifier.create(service.revokeInvitation("REV2", 300L, auth)).expectStatus(HttpStatus.BAD_REQUEST).verify()
    }

    // endregion

    // region acceptInvitation / declineInvitation

    @Test
    fun `acceptInvitation grants USER role to the caller`() {
        val auth = BackendFixtures.mockAuthentication(userId = 300L)
        val ps =
            BackendFixtures.problemSet(
                userIdRoleMap = mapOf(100L to UserRole.ADMIN),
                invitedUserIds = setOf(300L),
                shareCode = "A1",
            )
        every { problemSetRepository.findByShareCode("A1") } returns Mono.just(ps)
        every { problemSetRepository.save(any()) } answers { Mono.just(firstArg()) }

        StepVerifier.create(service.acceptInvitation("A1", auth))
            .assertNext {
                assert(it.getUserRole(300L) == UserRole.USER)
                assert(!it.isUserInvited(300L))
            }
            .verifyComplete()
    }

    @Test
    fun `acceptInvitation emits FORBIDDEN when caller has no invitation`() {
        val auth = BackendFixtures.mockAuthentication(userId = 300L)
        val ps = BackendFixtures.problemSet(invitedUserIds = emptySet(), shareCode = "A2")
        every { problemSetRepository.findByShareCode("A2") } returns Mono.just(ps)

        StepVerifier.create(service.acceptInvitation("A2", auth)).expectStatus(HttpStatus.FORBIDDEN).verify()
    }

    @Test
    fun `declineInvitation removes the pending invite without joining`() {
        val auth = BackendFixtures.mockAuthentication(userId = 300L)
        val ps =
            BackendFixtures.problemSet(
                userIdRoleMap = mapOf(100L to UserRole.ADMIN),
                invitedUserIds = setOf(300L),
                shareCode = "D1",
            )
        every { problemSetRepository.findByShareCode("D1") } returns Mono.just(ps)
        every { problemSetRepository.save(any()) } answers { Mono.just(firstArg()) }

        StepVerifier.create(service.declineInvitation("D1", auth))
            .assertNext {
                assert(!it.isUserInProblemSet(300L))
                assert(!it.isUserInvited(300L))
            }
            .verifyComplete()
    }

    // endregion

    private fun <T : Any> StepVerifier.FirstStep<T>.expectStatus(status: HttpStatus): StepVerifier = expectErrorMatches {
        it is ResponseStatusException && it.statusCode == status
    }
}
