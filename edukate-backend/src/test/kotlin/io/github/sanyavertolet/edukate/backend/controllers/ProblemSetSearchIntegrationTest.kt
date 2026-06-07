package io.github.sanyavertolet.edukate.backend.controllers

import io.github.sanyavertolet.edukate.backend.AbstractBackendIntegrationTest
import io.github.sanyavertolet.edukate.backend.BackendFixtures
import io.github.sanyavertolet.edukate.backend.dtos.ProblemSetMetadata
import io.github.sanyavertolet.edukate.backend.repositories.ProblemSetRepository
import io.github.sanyavertolet.edukate.backend.repositories.UserRepository
import io.github.sanyavertolet.edukate.common.users.UserRole
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.CacheManager
import org.springframework.test.web.reactive.server.expectBodyList

class ProblemSetSearchIntegrationTest : AbstractBackendIntegrationTest() {

    @Autowired private lateinit var userRepository: UserRepository
    @Autowired private lateinit var problemSetRepository: ProblemSetRepository
    @Autowired private lateinit var cacheManager: CacheManager

    private var memberId: Long = 0L
    private val memberName = "srch-member"
    private val memberShareCode = "SRCH-MEMBER"
    private val nonMemberShareCode = "SRCH-OTHER"

    @BeforeEach
    fun setUp() {
        val member = userRepository.save(BackendFixtures.user(id = null, name = memberName)).block()!!
        val other = userRepository.save(BackendFixtures.user(id = null, name = "srch-other")).block()!!
        memberId = requireNotNull(member.id)
        val otherId = requireNotNull(other.id)

        // Problem set the member belongs to
        problemSetRepository
            .save(
                BackendFixtures.problemSet(
                    id = null,
                    name = "Kinematics Practice",
                    shareCode = memberShareCode,
                    userIdRoleMap = mapOf(memberId to UserRole.USER),
                )
            )
            .block()

        // Problem set the member is NOT in — must never appear in search results
        problemSetRepository
            .save(
                BackendFixtures.problemSet(
                    id = null,
                    name = "Kinematics Other",
                    shareCode = nonMemberShareCode,
                    userIdRoleMap = mapOf(otherId to UserRole.ADMIN),
                )
            )
            .block()
    }

    @AfterEach
    fun tearDown() {
        cacheManager.cacheNames.forEach { cacheManager.getCache(it)?.clear() }
        problemSetRepository.deleteAll().block()
        userRepository.deleteAll().block()
    }

    @Test
    fun `searchMemberProblemSets returns matching set by name substring`() {
        val result =
            authenticatedClient(memberId, memberName)
                .get()
                .uri("/api/v1/problem-sets/search/member?query=Kinema")
                .exchange()
                .expectStatus()
                .isOk
                .expectBodyList<ProblemSetMetadata>()
                .returnResult()
                .responseBody
        assertThat(result).hasSize(1)
        assertThat(result!![0].shareCode).isEqualTo(memberShareCode)
    }

    @Test
    fun `searchMemberProblemSets matches by share code substring`() {
        val result =
            authenticatedClient(memberId, memberName)
                .get()
                .uri("/api/v1/problem-sets/search/member?query=SRCH-MEM")
                .exchange()
                .expectStatus()
                .isOk
                .expectBodyList<ProblemSetMetadata>()
                .returnResult()
                .responseBody
        assertThat(result).hasSize(1)
        assertThat(result!![0].shareCode).isEqualTo(memberShareCode)
    }

    @Test
    fun `searchMemberProblemSets excludes sets the user is not a member of`() {
        authenticatedClient(memberId, memberName)
            .get()
            .uri("/api/v1/problem-sets/search/member?query=Other")
            .exchange()
            .expectStatus()
            .isOk
            .expectBodyList<ProblemSetMetadata>()
            .hasSize(0)
    }

    @Test
    fun `searchMemberProblemSets is case insensitive`() {
        authenticatedClient(memberId, memberName)
            .get()
            .uri("/api/v1/problem-sets/search/member?query=kinema")
            .exchange()
            .expectStatus()
            .isOk
            .expectBodyList<ProblemSetMetadata>()
            .hasSize(1)
    }
}
