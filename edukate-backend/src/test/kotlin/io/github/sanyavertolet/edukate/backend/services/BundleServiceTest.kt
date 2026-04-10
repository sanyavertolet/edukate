@file:Suppress("ReactiveStreamsUnusedPublisher")

package io.github.sanyavertolet.edukate.backend.services

import io.github.sanyavertolet.edukate.backend.BackendFixtures
import io.github.sanyavertolet.edukate.backend.permissions.BundlePermissionEvaluator
import io.github.sanyavertolet.edukate.backend.repositories.BundleRepository
import io.github.sanyavertolet.edukate.common.users.UserRole
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.test.StepVerifier

class BundleServiceTest {

    private val bundleRepository: BundleRepository = mockk()
    private val shareCodeGenerator: ShareCodeGenerator = mockk()
    private val problemService: ProblemService = mockk()
    private val bundlePermissionEvaluator: BundlePermissionEvaluator = mockk()
    private val userService: UserService = mockk()
    private lateinit var service: BundleService

    @BeforeEach
    fun setUp() {
        service = BundleService(bundleRepository, shareCodeGenerator, problemService, bundlePermissionEvaluator, userService)
    }

    // region findBundleByShareCode

    @Test
    fun `findBundleByShareCode returns bundle when found`() {
        val bundle = BackendFixtures.bundle(shareCode = "CODE1")
        every { bundleRepository.findBundleByShareCode("CODE1") } returns Mono.just(bundle)

        StepVerifier.create(service.findBundleByShareCode("CODE1")).expectNext(bundle).verifyComplete()
    }

    @Test
    fun `findBundleByShareCode emits NOT_FOUND when share code is unknown`() {
        every { bundleRepository.findBundleByShareCode("UNKNOWN") } returns Mono.empty()

        StepVerifier.create(service.findBundleByShareCode("UNKNOWN"))
            .expectErrorMatches { it is ResponseStatusException && it.statusCode == HttpStatus.NOT_FOUND }
            .verify()
    }

    // endregion

    // region getOwnedBundles / getJoinedBundles / getPublicBundles

    @Test
    fun `getOwnedBundles queries repository with ADMIN role`() {
        val auth = BackendFixtures.mockAuthentication(userId = "user-1")
        val bundle = BackendFixtures.bundle()
        every { bundleRepository.findBundlesByUserRoleIn("user-1", listOf(UserRole.ADMIN), any()) } returns Flux.just(bundle)

        StepVerifier.create(service.getOwnedBundles(mockk(relaxed = true), auth)).expectNext(bundle).verifyComplete()
    }

    @Test
    fun `getJoinedBundles queries repository with all roles`() {
        val auth = BackendFixtures.mockAuthentication(userId = "user-1")
        val bundle = BackendFixtures.bundle()
        every { bundleRepository.findBundlesByUserRoleIn("user-1", UserRole.anyRole(), any()) } returns Flux.just(bundle)

        StepVerifier.create(service.getJoinedBundles(mockk(relaxed = true), auth)).expectNext(bundle).verifyComplete()
    }

    @Test
    fun `getPublicBundles returns only public bundles`() {
        val bundle = BackendFixtures.bundle(isPublic = true)
        every { bundleRepository.findBundlesByIsPublic(true, any()) } returns Flux.just(bundle)

        StepVerifier.create(service.getPublicBundles(mockk(relaxed = true))).expectNext(bundle).verifyComplete()
    }

    // endregion

    // region createBundle

    @Test
    fun `createBundle saves bundle with authenticated user as ADMIN`() {
        val auth = BackendFixtures.mockAuthentication(userId = "user-1")
        val request =
            BackendFixtures.bundle().let {
                io.github.sanyavertolet.edukate.backend.dtos.CreateBundleRequest(
                    name = it.name,
                    description = it.description,
                    isPublic = it.isPublic,
                    problemIds = it.problemIds,
                )
            }
        every { shareCodeGenerator.generateShareCode() } returns "NEWCODE"
        every { bundleRepository.save(any()) } answers { Mono.just(firstArg()) }

        StepVerifier.create(service.createBundle(request, auth))
            .assertNext { saved ->
                assert(saved.userIdRoleMap["user-1"] == UserRole.ADMIN)
                assert(saved.shareCode == "NEWCODE")
            }
            .verifyComplete()
    }

    // endregion

    // region joinUser

    @Test
    fun `joinUser adds user to bundle when allowed`() {
        val bundle = BackendFixtures.bundle(isPublic = true, shareCode = "JOIN1")
        every { bundleRepository.findBundleByShareCode("JOIN1") } returns Mono.just(bundle)
        every { bundlePermissionEvaluator.hasJoinPermission(bundle, "new-user") } returns true
        every { bundleRepository.save(any()) } answers { Mono.just(firstArg()) }

        StepVerifier.create(service.joinUser("JOIN1", "new-user"))
            .assertNext { updated -> assert(updated.isUserInBundle("new-user")) }
            .verifyComplete()
    }

    @Test
    fun `joinUser emits FORBIDDEN when user has no join permission`() {
        val bundle = BackendFixtures.bundle(isPublic = false, invitedUserIds = emptySet(), shareCode = "JOIN2")
        every { bundleRepository.findBundleByShareCode("JOIN2") } returns Mono.just(bundle)
        every { bundlePermissionEvaluator.hasJoinPermission(bundle, "stranger") } returns false

        StepVerifier.create(service.joinUser("JOIN2", "stranger"))
            .expectErrorMatches { it is ResponseStatusException && it.statusCode == HttpStatus.FORBIDDEN }
            .verify()
    }

    @Test
    fun `joinUser emits BAD_REQUEST when user is already in bundle`() {
        val bundle =
            BackendFixtures.bundle(
                isPublic = true,
                userIdRoleMap = mapOf("admin-1" to UserRole.ADMIN, "user-1" to UserRole.USER),
                shareCode = "JOIN3",
            )
        every { bundleRepository.findBundleByShareCode("JOIN3") } returns Mono.just(bundle)
        every { bundlePermissionEvaluator.hasJoinPermission(bundle, "user-1") } returns true

        StepVerifier.create(service.joinUser("JOIN3", "user-1"))
            .expectErrorMatches { it is ResponseStatusException && it.statusCode == HttpStatus.BAD_REQUEST }
            .verify()
    }

    // endregion

    // region removeUser

    @Test
    fun `removeUser removes user from bundle`() {
        val bundle =
            BackendFixtures.bundle(
                userIdRoleMap = mapOf("admin-1" to UserRole.ADMIN, "user-1" to UserRole.USER),
                shareCode = "REM1",
            )
        every { bundleRepository.findBundleByShareCode("REM1") } returns Mono.just(bundle)
        every { bundleRepository.save(any()) } answers { Mono.just(firstArg()) }

        StepVerifier.create(service.removeUser("REM1", "user-1"))
            .assertNext { updated -> assert(!updated.isUserInBundle("user-1")) }
            .verifyComplete()
    }

    @Test
    fun `removeUser emits BAD_REQUEST when user is not in bundle`() {
        val bundle = BackendFixtures.bundle(userIdRoleMap = mapOf("admin-1" to UserRole.ADMIN), shareCode = "REM2")
        every { bundleRepository.findBundleByShareCode("REM2") } returns Mono.just(bundle)

        StepVerifier.create(service.removeUser("REM2", "nobody"))
            .expectErrorMatches { it is ResponseStatusException && it.statusCode == HttpStatus.BAD_REQUEST }
            .verify()
    }

    @Test
    fun `removeUser emits BAD_REQUEST when last admin tries to leave`() {
        val bundle = BackendFixtures.bundle(userIdRoleMap = mapOf("admin-1" to UserRole.ADMIN), shareCode = "REM3")
        every { bundleRepository.findBundleByShareCode("REM3") } returns Mono.just(bundle)

        StepVerifier.create(service.removeUser("REM3", "admin-1"))
            .expectErrorMatches { it is ResponseStatusException && it.statusCode == HttpStatus.BAD_REQUEST }
            .verify()
    }

    // endregion

    // region inviteUser

    @Test
    fun `inviteUser adds user to invitedUserIds`() {
        val bundle =
            BackendFixtures.bundle(
                userIdRoleMap = mapOf("admin-1" to UserRole.ADMIN),
                invitedUserIds = emptySet(),
                shareCode = "INV1",
            )
        every { bundleRepository.findBundleByShareCode("INV1") } returns Mono.just(bundle)
        every { bundlePermissionEvaluator.hasInvitePermission(bundle, "admin-1") } returns true
        every { bundleRepository.save(any()) } answers { Mono.just(firstArg()) }

        StepVerifier.create(service.inviteUser("INV1", "admin-1", "new-user"))
            .assertNext { updated -> assert(updated.isUserInvited("new-user")) }
            .verifyComplete()
    }

    @Test
    fun `inviteUser emits FORBIDDEN when requester lacks invite permission`() {
        val bundle =
            BackendFixtures.bundle(
                userIdRoleMap = mapOf("admin-1" to UserRole.ADMIN, "user-1" to UserRole.USER),
                shareCode = "INV2",
            )
        every { bundleRepository.findBundleByShareCode("INV2") } returns Mono.just(bundle)
        every { bundlePermissionEvaluator.hasInvitePermission(bundle, "user-1") } returns false

        StepVerifier.create(service.inviteUser("INV2", "user-1", "new-user"))
            .expectErrorMatches { it is ResponseStatusException && it.statusCode == HttpStatus.FORBIDDEN }
            .verify()
    }

    @Test
    fun `inviteUser emits BAD_REQUEST when invitee is already in bundle`() {
        val bundle =
            BackendFixtures.bundle(
                userIdRoleMap = mapOf("admin-1" to UserRole.ADMIN, "existing-user" to UserRole.USER),
                shareCode = "INV3",
            )
        every { bundleRepository.findBundleByShareCode("INV3") } returns Mono.just(bundle)
        every { bundlePermissionEvaluator.hasInvitePermission(bundle, "admin-1") } returns true

        StepVerifier.create(service.inviteUser("INV3", "admin-1", "existing-user"))
            .expectErrorMatches { it is ResponseStatusException && it.statusCode == HttpStatus.BAD_REQUEST }
            .verify()
    }

    // endregion

    // region changeVisibility

    @Test
    fun `changeVisibility sets isPublic when requester has MODERATOR role`() {
        val auth = BackendFixtures.mockAuthentication(userId = "admin-1")
        val bundle = BackendFixtures.bundle(isPublic = false, shareCode = "VIS1")
        every { bundleRepository.findBundleByShareCode("VIS1") } returns Mono.just(bundle)
        every { bundlePermissionEvaluator.hasRole(bundle, UserRole.MODERATOR, auth) } returns true
        every { bundleRepository.save(any()) } answers { Mono.just(firstArg()) }

        StepVerifier.create(service.changeVisibility("VIS1", true, auth))
            .assertNext { updated -> assert(updated.isPublic) }
            .verifyComplete()
    }

    @Test
    fun `changeVisibility emits FORBIDDEN when requester lacks MODERATOR role`() {
        val auth = BackendFixtures.mockAuthentication(userId = "user-1")
        val bundle = BackendFixtures.bundle(shareCode = "VIS2")
        every { bundleRepository.findBundleByShareCode("VIS2") } returns Mono.just(bundle)
        every { bundlePermissionEvaluator.hasRole(bundle, UserRole.MODERATOR, auth) } returns false

        StepVerifier.create(service.changeVisibility("VIS2", true, auth))
            .expectErrorMatches { it is ResponseStatusException && it.statusCode == HttpStatus.FORBIDDEN }
            .verify()
    }

    // endregion

    // region changeProblems

    @Test
    fun `changeProblems emits BAD_REQUEST when problem list is empty`() {
        val auth = BackendFixtures.mockAuthentication()

        StepVerifier.create(service.changeProblems("PROB1", emptyList(), auth))
            .expectErrorMatches { it is ResponseStatusException && it.statusCode == HttpStatus.BAD_REQUEST }
            .verify()

        verify(exactly = 0) { bundleRepository.findBundleByShareCode(any()) }
    }

    @Test
    fun `changeProblems updates problemIds when authorized`() {
        val auth = BackendFixtures.mockAuthentication()
        val bundle = BackendFixtures.bundle(shareCode = "PROB2")
        every { bundleRepository.findBundleByShareCode("PROB2") } returns Mono.just(bundle)
        every { bundlePermissionEvaluator.hasRole(bundle, UserRole.MODERATOR, auth) } returns true
        every { bundleRepository.save(any()) } answers { Mono.just(firstArg()) }

        StepVerifier.create(service.changeProblems("PROB2", listOf("1.0.0", "2.0.0"), auth))
            .assertNext { updated -> assert(updated.problemIds == listOf("1.0.0", "2.0.0")) }
            .verifyComplete()
    }

    // endregion
}
