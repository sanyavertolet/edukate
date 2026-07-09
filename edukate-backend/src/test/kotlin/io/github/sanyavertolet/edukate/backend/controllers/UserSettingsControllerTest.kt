@file:Suppress("ReactiveStreamsUnusedPublisher")

package io.github.sanyavertolet.edukate.backend.controllers

import com.ninjasquad.springmockk.MockkBean
import io.github.sanyavertolet.edukate.backend.BackendFixtures
import io.github.sanyavertolet.edukate.backend.mappers.UserMapper
import io.github.sanyavertolet.edukate.backend.services.AuthTokenService
import io.github.sanyavertolet.edukate.backend.services.UserService
import io.github.sanyavertolet.edukate.backend.services.files.FileManager
import io.github.sanyavertolet.edukate.common.security.NoopWebSecurityConfig
import io.github.sanyavertolet.edukate.storage.keys.UserAvatarFileKey
import io.mockk.every
import io.mockk.verify
import java.time.Instant
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.http.client.MultipartBodyBuilder
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.reactive.function.BodyInserters
import reactor.core.publisher.Mono

@WebFluxTest(UserSettingsController::class)
@Import(NoopWebSecurityConfig::class)
class UserSettingsControllerTest {

    @Autowired private lateinit var webTestClient: WebTestClient

    @MockkBean private lateinit var userService: UserService
    @MockkBean private lateinit var fileManager: FileManager
    @MockkBean private lateinit var authTokenService: AuthTokenService
    @MockkBean private lateinit var userMapper: UserMapper

    private fun authenticated(): WebTestClient =
        webTestClient.mutateWith(
            SecurityMockServerConfigurers.mockAuthentication(BackendFixtures.mockAuthentication(userId = 42L))
        )

    // region PUT /api/v1/users/me/avatar

    @Test
    fun `uploadAvatar uploads to fixed key, marks updated, returns public url`() {
        val updatedUser = BackendFixtures.user(id = 42L, avatarUpdatedAt = Instant.ofEpochSecond(1_700_000_000))
        val expectedUrl = "http://minio:9000/edukate/users/42/avatar/avatar.jpg?v=1700000000"

        every { fileManager.uploadFile(UserAvatarFileKey(42L), any(), any()) } returns Mono.just(UserAvatarFileKey(42L))
        every { userService.setAvatarTimestamp(42L, any()) } returns Mono.just(updatedUser)
        every { userMapper.avatarUrl(updatedUser) } returns expectedUrl

        val body = MultipartBodyBuilder()
        body.part("content", "fake-jpeg-bytes".toByteArray())

        authenticated()
            .put()
            .uri("/api/v1/users/me/avatar")
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .body(BodyInserters.fromMultipartData(body.build()))
            .exchange()
            .expectStatus()
            .isOk
            .expectBody(String::class.java)
            .isEqualTo(expectedUrl)

        verify { fileManager.uploadFile(UserAvatarFileKey(42L), MediaType.IMAGE_JPEG, any()) }
        verify { userService.setAvatarTimestamp(42L, any()) }
    }

    @Test
    fun `uploadAvatar never persists the timestamp when the S3 upload fails`() {
        every { fileManager.uploadFile(UserAvatarFileKey(42L), any(), any()) } returns
            Mono.error(RuntimeException("s3 unavailable"))
        // `.then(...)` evaluates its argument eagerly, so the method is *called*; the invariant
        // that matters is that its Mono is never *subscribed* (no DB write). Track subscription.
        var timestampSubscribed = false
        every { userService.setAvatarTimestamp(any(), any()) } returns
            Mono.fromCallable { BackendFixtures.user(id = 42L) }.doOnSubscribe { timestampSubscribed = true }

        val body = MultipartBodyBuilder()
        body.part("content", "fake-jpeg-bytes".toByteArray())

        authenticated()
            .put()
            .uri("/api/v1/users/me/avatar")
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .body(BodyInserters.fromMultipartData(body.build()))
            .exchange()
            .expectStatus()
            .is5xxServerError

        assertThat(timestampSubscribed).isFalse()
        // The URL mapping runs only on a successful emission, which never happens here.
        verify(exactly = 0) { userMapper.avatarUrl(any()) }
    }

    // endregion

    // region DELETE /api/v1/users/me/avatar

    @Test
    fun `deleteAvatar removes S3 blob and clears avatar timestamp`() {
        every { fileManager.deleteFile(UserAvatarFileKey(42L)) } returns Mono.just(true)
        every { userService.setAvatarTimestamp(42L, null) } returns Mono.just(BackendFixtures.user(id = 42L))

        authenticated().delete().uri("/api/v1/users/me/avatar").exchange().expectStatus().isNoContent

        verify { fileManager.deleteFile(UserAvatarFileKey(42L)) }
        verify { userService.setAvatarTimestamp(42L, null) }
    }

    @Test
    fun `deleteAvatar still clears the timestamp when the blob was already absent`() {
        every { fileManager.deleteFile(UserAvatarFileKey(42L)) } returns Mono.just(false)
        every { userService.setAvatarTimestamp(42L, null) } returns Mono.just(BackendFixtures.user(id = 42L))

        authenticated().delete().uri("/api/v1/users/me/avatar").exchange().expectStatus().isNoContent

        verify { userService.setAvatarTimestamp(42L, null) }
    }

    // endregion

    // region POST /api/v1/users/me/email-change

    @Test
    fun `requestEmailChange dispatches a verification token to the new email`() {
        every { userService.findUserById(42L) } returns Mono.just(BackendFixtures.user(id = 42L))
        every { authTokenService.issueVerificationToken(42L, "new@example.com") } returns Mono.empty()

        authenticated()
            .post()
            .uri("/api/v1/users/me/email-change")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(mapOf("newEmail" to "new@example.com"))
            .exchange()
            .expectStatus()
            .isAccepted

        verify { authTokenService.issueVerificationToken(42L, "new@example.com") }
    }

    @Test
    fun `requestEmailChange rejects an invalid email shape`() {
        authenticated()
            .post()
            .uri("/api/v1/users/me/email-change")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(mapOf("newEmail" to "not-an-email"))
            .exchange()
            .expectStatus()
            .isBadRequest
    }

    // endregion
}
