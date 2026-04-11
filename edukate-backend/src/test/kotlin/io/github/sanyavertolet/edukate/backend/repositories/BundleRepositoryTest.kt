package io.github.sanyavertolet.edukate.backend.repositories

import io.github.sanyavertolet.edukate.backend.BackendFixtures
import io.github.sanyavertolet.edukate.backend.MongoTestContainerConfig
import io.github.sanyavertolet.edukate.backend.configs.MongoConfig
import io.github.sanyavertolet.edukate.common.users.UserRole
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.mongodb.test.autoconfigure.DataMongoTest
import org.springframework.context.annotation.Import
import org.springframework.data.domain.PageRequest
import reactor.test.StepVerifier

@DataMongoTest
@Import(MongoConfig::class, MongoTestContainerConfig::class)
class BundleRepositoryTest {

    @Autowired private lateinit var repository: BundleRepository

    @BeforeEach
    fun cleanUp() {
        repository.deleteAll().block()
    }

    // region findBundleByShareCode

    @Test
    fun `findBundleByShareCode returns bundle for matching share code`() {
        repository.save(BackendFixtures.bundle(id = null, shareCode = "FIND1")).block()

        StepVerifier.create(repository.findBundleByShareCode("FIND1"))
            .assertNext { assertThat(it.shareCode).isEqualTo("FIND1") }
            .verifyComplete()
    }

    @Test
    fun `findBundleByShareCode returns empty Mono for unknown share code`() {
        StepVerifier.create(repository.findBundleByShareCode("UNKNOWN")).verifyComplete()
    }

    @Test
    fun `findBundleByShareCode does not return bundles with a different share code`() {
        repository.save(BackendFixtures.bundle(id = null, shareCode = "CODE-A")).block()

        StepVerifier.create(repository.findBundleByShareCode("CODE-B")).verifyComplete()
    }

    // endregion

    // region findBundlesByIsPublic

    @Test
    fun `findBundlesByIsPublic returns only public bundles`() {
        repository.save(BackendFixtures.bundle(id = null, shareCode = "PUB1", isPublic = true)).block()
        repository.save(BackendFixtures.bundle(id = null, shareCode = "PRV1", isPublic = false)).block()

        val pageable = PageRequest.of(0, 10)

        StepVerifier.create(repository.findBundlesByIsPublic(true, pageable))
            .assertNext { assertThat(it.isPublic).isTrue() }
            .verifyComplete()
    }

    @Test
    fun `findBundlesByIsPublic returns empty flux when no public bundles exist`() {
        repository.save(BackendFixtures.bundle(id = null, shareCode = "PRV2", isPublic = false)).block()

        val pageable = PageRequest.of(0, 10)

        StepVerifier.create(repository.findBundlesByIsPublic(true, pageable)).verifyComplete()
    }

    @Test
    fun `findBundlesByIsPublic respects page size`() {
        repeat(5) { i -> repository.save(BackendFixtures.bundle(id = null, shareCode = "PUB-$i", isPublic = true)).block() }

        val pageable = PageRequest.of(0, 3)

        StepVerifier.create(repository.findBundlesByIsPublic(true, pageable)).expectNextCount(3).verifyComplete()
    }

    // endregion

    // region findBundlesByUserRoleIn

    @Test
    fun `findBundlesByUserRoleIn returns bundles where user has one of the specified roles`() {
        repository
            .save(BackendFixtures.bundle(id = null, shareCode = "ROLE1", userIdRoleMap = mapOf("user-1" to UserRole.ADMIN)))
            .block()
        repository
            .save(BackendFixtures.bundle(id = null, shareCode = "ROLE2", userIdRoleMap = mapOf("user-1" to UserRole.USER)))
            .block()

        val pageable = PageRequest.of(0, 10)

        StepVerifier.create(repository.findBundlesByUserRoleIn("user-1", listOf(UserRole.ADMIN), pageable))
            .assertNext { assertThat(it.shareCode).isEqualTo("ROLE1") }
            .verifyComplete()
    }

    @Test
    fun `findBundlesByUserRoleIn returns bundles for multiple matching roles`() {
        repository
            .save(BackendFixtures.bundle(id = null, shareCode = "MULTI1", userIdRoleMap = mapOf("user-1" to UserRole.ADMIN)))
            .block()
        repository
            .save(BackendFixtures.bundle(id = null, shareCode = "MULTI2", userIdRoleMap = mapOf("user-1" to UserRole.USER)))
            .block()
        repository
            .save(BackendFixtures.bundle(id = null, shareCode = "MULTI3", userIdRoleMap = mapOf("other" to UserRole.ADMIN)))
            .block()

        val pageable = PageRequest.of(0, 10)

        StepVerifier.create(repository.findBundlesByUserRoleIn("user-1", listOf(UserRole.ADMIN, UserRole.USER), pageable))
            .expectNextCount(2)
            .verifyComplete()
    }

    @Test
    fun `findBundlesByUserRoleIn does not return bundles for other users`() {
        repository
            .save(
                BackendFixtures.bundle(
                    id = null,
                    shareCode = "OTHER1",
                    userIdRoleMap = mapOf("other-user" to UserRole.ADMIN),
                )
            )
            .block()

        val pageable = PageRequest.of(0, 10)

        StepVerifier.create(repository.findBundlesByUserRoleIn("target-user", UserRole.anyRole(), pageable)).verifyComplete()
    }

    @Test
    fun `findBundlesByUserRoleIn returns empty flux when user has no matching role`() {
        repository
            .save(BackendFixtures.bundle(id = null, shareCode = "NOROLE1", userIdRoleMap = mapOf("user-1" to UserRole.USER)))
            .block()

        val pageable = PageRequest.of(0, 10)

        StepVerifier.create(repository.findBundlesByUserRoleIn("user-1", listOf(UserRole.ADMIN), pageable)).verifyComplete()
    }

    // endregion
}
