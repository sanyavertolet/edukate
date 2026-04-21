@file:Suppress("ReactiveStreamsUnusedPublisher")

package io.github.sanyavertolet.edukate.backend.controllers.files

import com.ninjasquad.springmockk.MockkBean
import io.github.sanyavertolet.edukate.backend.BackendFixtures
import io.github.sanyavertolet.edukate.backend.dtos.FileMetadata
import io.github.sanyavertolet.edukate.backend.services.files.FileManager
import io.github.sanyavertolet.edukate.common.security.NoopWebSecurityConfig
import io.github.sanyavertolet.edukate.storage.keys.TempFileKey
import io.mockk.every
import java.nio.ByteBuffer
import java.time.Instant
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.http.client.MultipartBodyBuilder
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.reactive.function.BodyInserters
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@WebFluxTest(TempFileController::class)
@Import(NoopWebSecurityConfig::class)
class TempFileControllerTest {

    @Autowired private lateinit var webTestClient: WebTestClient

    @MockkBean private lateinit var fileManager: FileManager

    private fun authenticatedClient(): WebTestClient =
        webTestClient.mutateWith(
            SecurityMockServerConfigurers.mockAuthentication(BackendFixtures.mockAuthentication(userId = 1L))
        )

    // region POST /api/v1/files/temp

    @Test
    fun `uploadTempFile returns 200 with fileName`() {
        every { fileManager.uploadFile(any(), any(), any()) } returns Mono.just(TempFileKey(1L, "test-file.txt"))

        val body = MultipartBodyBuilder()
        body.part("content", "file content".toByteArray())

        authenticatedClient()
            .post()
            .uri("/api/v1/files/temp")
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .body(BodyInserters.fromMultipartData(body.build()))
            .exchange()
            .expectStatus()
            .isOk
            .expectBody(String::class.java)
            .isEqualTo("test-file.txt")
    }

    // endregion

    // region DELETE /api/v1/files/temp

    @Test
    fun `deleteTempFile returns 200 with fileName when deletion succeeds`() {
        every { fileManager.deleteFile(any()) } returns Mono.just(true)

        authenticatedClient()
            .delete()
            .uri("/api/v1/files/temp?fileName=test-file.txt")
            .exchange()
            .expectStatus()
            .isOk
            .expectBody(String::class.java)
            .isEqualTo("test-file.txt")
    }

    @Test
    fun `deleteTempFile returns 500 when deletion fails`() {
        every { fileManager.deleteFile(any()) } returns Mono.just(false)

        authenticatedClient()
            .delete()
            .uri("/api/v1/files/temp?fileName=test-file.txt")
            .exchange()
            .expectStatus()
            .is5xxServerError
    }

    // endregion

    // region GET /api/v1/files/temp/get

    @Test
    fun `downloadTempFile returns 200 with file content`() {
        every { fileManager.getFileContent(any()) } returns Flux.just(ByteBuffer.wrap("file content".toByteArray()))

        authenticatedClient().get().uri("/api/v1/files/temp/get?fileName=test-file.txt").exchange().expectStatus().isOk
    }

    @Test
    fun `downloadTempFile returns 404 when file not found`() {
        every { fileManager.getFileContent(any()) } returns Flux.empty()

        authenticatedClient().get().uri("/api/v1/files/temp/get?fileName=missing.txt").exchange().expectStatus().isNotFound
    }

    // endregion

    // region GET /api/v1/files/temp

    @Test
    fun `getTempFiles returns 200 with list of file metadata`() {
        every { fileManager.listFileMetadataWithPrefix(any(), any()) } returns
            Flux.just(FileMetadata("key/test-file.txt", "testuser", Instant.now(), 100L))

        authenticatedClient()
            .get()
            .uri("/api/v1/files/temp")
            .exchange()
            .expectStatus()
            .isOk
            .expectBodyList(FileMetadata::class.java)
            .hasSize(1)
    }

    // endregion
}
