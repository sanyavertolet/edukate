@file:Suppress("ReactiveStreamsUnusedPublisher")

package io.github.sanyavertolet.edukate.backend.services

import io.github.sanyavertolet.edukate.backend.entities.files.FileObject
import io.github.sanyavertolet.edukate.backend.entities.files.FileObjectMetadata
import io.github.sanyavertolet.edukate.backend.repositories.FileObjectRepository
import io.github.sanyavertolet.edukate.backend.services.files.FileManager
import io.github.sanyavertolet.edukate.backend.storage.FileKeyStorage
import io.github.sanyavertolet.edukate.storage.keys.ProblemFileKey
import io.github.sanyavertolet.edukate.storage.keys.SubmissionFileKey
import io.github.sanyavertolet.edukate.storage.keys.TempFileKey
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.nio.ByteBuffer
import java.time.Duration
import java.time.Instant
import java.util.concurrent.TimeoutException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.test.StepVerifier

class FileManagerTest {
    private val fileObjectRepository: FileObjectRepository = mockk()
    private val storage: FileKeyStorage = mockk()
    private lateinit var fileManager: FileManager

    @BeforeEach
    fun setUp() {
        fileManager = FileManager(fileObjectRepository, storage)
    }

    private fun meta(size: Long = 100L, contentType: String = "text/plain") =
        FileObjectMetadata(Instant.now(), size, contentType)

    private fun fileObject(key: ProblemFileKey, meta: FileObjectMetadata = meta()) =
        FileObject(id = 1L, keyPath = key.toString(), key = key, type = "problem", ownerUserId = 0L, metadata = meta)

    // region getFileObject

    @Test
    fun `getFileObject returns from repo`() {
        val key = ProblemFileKey("savchenko", "1.1.1", "image.png")
        val fo = fileObject(key)
        every { fileObjectRepository.findByKeyPath(key.toString()) } returns Mono.just(fo)

        StepVerifier.create(fileManager.getFileObject(key)).expectNext(fo).verifyComplete()
    }

    @Test
    fun `getFileObject times out`() {
        val key = ProblemFileKey("savchenko", "1.1.1", "image.png")
        every { fileObjectRepository.findByKeyPath(key.toString()) } returns Mono.never()

        StepVerifier.withVirtualTime { fileManager.getFileObject(key) }
            .thenAwait(Duration.ofSeconds(31))
            .expectError(TimeoutException::class.java)
            .verify()
    }

    // endregion

    // region getFileContent

    @Test
    fun `getFileContent downloads from storage`() {
        val key = ProblemFileKey("savchenko", "1.1.1", "image.png")
        val buffer = ByteBuffer.wrap(byteArrayOf(1, 2, 3))
        every { storage.getContent(key) } returns Flux.just(buffer)

        StepVerifier.create(fileManager.getFileContent(key)).expectNext(buffer).verifyComplete()
    }

    // endregion

    // region getPresignedUrl

    @Test
    fun `getPresignedUrl delegates to storage`() {
        val key = ProblemFileKey("savchenko", "1.1.1", "image.png")
        every { storage.generatePresignedUrl(key) } returns Mono.just("https://s3/presigned-url")

        StepVerifier.create(fileManager.getPresignedUrl(key)).expectNext("https://s3/presigned-url").verifyComplete()
    }

    // endregion

    // region uploadFile

    @Test
    fun `uploadFile saves Metadata to db`() {
        val key = TempFileKey(1L, "file.txt")
        val metadata = meta()
        val content = Flux.empty<ByteBuffer>()

        every { storage.upload(key, "text/plain", content) } returns Mono.just(key)
        every { storage.metadata(key) } returns Mono.just(metadata)
        every { fileObjectRepository.findByKeyPath(key.toString()) } returns Mono.empty()
        every { fileObjectRepository.save(any()) } answers { Mono.just(firstArg()) }

        StepVerifier.create(fileManager.uploadFile(key, org.springframework.http.MediaType.TEXT_PLAIN, content))
            .expectNext(key)
            .verifyComplete()

        verify(exactly = 1) { fileObjectRepository.save(any()) }
    }

    @Test
    fun `uploadFile updates existing file object`() {
        val key = TempFileKey(1L, "file.txt")
        val metadata = meta()
        val content = Flux.empty<ByteBuffer>()
        val existingFo =
            FileObject(id = 10L, keyPath = key.toString(), key = key, type = "tmp", ownerUserId = 1L, metadata = meta(50L))

        every { storage.upload(key, "text/plain", content) } returns Mono.just(key)
        every { storage.metadata(key) } returns Mono.just(metadata)
        every { fileObjectRepository.findByKeyPath(key.toString()) } returns Mono.just(existingFo)
        every { fileObjectRepository.save(any()) } answers { Mono.just(firstArg()) }

        StepVerifier.create(fileManager.uploadFile(key, org.springframework.http.MediaType.TEXT_PLAIN, content))
            .expectNext(key)
            .verifyComplete()

        verify(exactly = 1) { fileObjectRepository.save(match { it.id == 10L }) }
    }

    // endregion

    // region deleteFile

    @Test
    fun `deleteFile both storage and db`() {
        val key = ProblemFileKey("savchenko", "1.1.1", "img.png")
        every { storage.delete(key) } returns Mono.just(true)
        every { fileObjectRepository.deleteByKeyPath(key.toString()) } returns Mono.just(1L)

        StepVerifier.create(fileManager.deleteFile(key)).expectNext(true).verifyComplete()
    }

    @Test
    fun `deleteFile storage fails continues to db`() {
        val key = ProblemFileKey("savchenko", "1.1.1", "img.png")
        every { storage.delete(key) } returns Mono.error(RuntimeException("S3 unavailable"))
        every { fileObjectRepository.deleteByKeyPath(key.toString()) } returns Mono.just(1L)

        StepVerifier.create(fileManager.deleteFile(key)).expectNext(true).verifyComplete()
    }

    @Test
    fun `deleteFile no record is idempotent`() {
        val key = ProblemFileKey("savchenko", "1.1.1", "img.png")
        every { storage.delete(key) } returns Mono.just(false)
        every { fileObjectRepository.deleteByKeyPath(key.toString()) } returns Mono.just(0L)

        StepVerifier.create(fileManager.deleteFile(key)).expectNext(false).verifyComplete()
    }

    // endregion

    // region doesFileExist / doFilesExist

    @Test
    fun `doesFileExist returns true when Metadata exists`() {
        val key = ProblemFileKey("savchenko", "1.1.1", "img.png")
        every { storage.metadata(key) } returns Mono.just(meta())

        StepVerifier.create(fileManager.doesFileExist(key)).expectNext(true).verifyComplete()
    }

    @Test
    fun `doesFileExist returns false when empty`() {
        val key = ProblemFileKey("savchenko", "1.1.1", "img.png")
        every { storage.metadata(key) } returns Mono.empty()

        StepVerifier.create(fileManager.doesFileExist(key)).expectNext(false).verifyComplete()
    }

    @Test
    fun `doFilesExist all exist`() {
        val key1 = ProblemFileKey("savchenko", "1.1.1", "img1.png")
        val key2 = ProblemFileKey("savchenko", "1.1.1", "img2.png")
        every { storage.metadata(key1) } returns Mono.just(meta())
        every { storage.metadata(key2) } returns Mono.just(meta(200L))

        StepVerifier.create(fileManager.doFilesExist(listOf(key1, key2))).expectNext(true).verifyComplete()
    }

    @Test
    fun `doFilesExist one missing`() {
        val key1 = ProblemFileKey("savchenko", "1.1.1", "img1.png")
        val key2 = ProblemFileKey("savchenko", "1.1.1", "missing.png")
        every { storage.metadata(key1) } returns Mono.just(meta())
        every { storage.metadata(key2) } returns Mono.empty()

        StepVerifier.create(fileManager.doFilesExist(listOf(key1, key2))).expectNext(false).verifyComplete()
    }

    // endregion

    // region moveFile

    @Test
    fun `moveFile updates KeyPath`() {
        val oldKey = TempFileKey(1L, "draft.txt")
        val newKey = SubmissionFileKey(1L, 1L, 1L, "draft.txt")
        val metadata = meta()

        every { storage.move(oldKey, newKey) } returns Mono.just(true)
        every { storage.metadata(newKey) } returns Mono.just(metadata)
        every { fileObjectRepository.findByKeyPath(oldKey.toString()) } returns Mono.empty()
        every { fileObjectRepository.findByKeyPath(newKey.toString()) } returns Mono.empty()
        every { fileObjectRepository.save(any()) } answers { Mono.just(firstArg()) }

        StepVerifier.create(fileManager.moveFile(oldKey, newKey)).expectNext(newKey).verifyComplete()
    }

    @Test
    fun `moveFile reuses existing file object when old path missing but new path already exists`() {
        val oldKey = TempFileKey(1L, "draft.txt")
        val newKey = SubmissionFileKey(1L, 1L, 1L, "draft.txt")
        val metadata = meta()
        val existingFo =
            FileObject(
                id = 10L,
                keyPath = newKey.toString(),
                key = newKey,
                type = "submission",
                ownerUserId = 1L,
                metadata = meta(50L),
            )

        every { storage.move(oldKey, newKey) } returns Mono.just(true)
        every { storage.metadata(newKey) } returns Mono.just(metadata)
        every { fileObjectRepository.findByKeyPath(oldKey.toString()) } returns Mono.empty()
        every { fileObjectRepository.findByKeyPath(newKey.toString()) } returns Mono.just(existingFo)
        every { fileObjectRepository.save(any()) } answers { Mono.just(firstArg()) }

        StepVerifier.create(fileManager.moveFile(oldKey, newKey)).expectNext(newKey).verifyComplete()

        verify(exactly = 1) { fileObjectRepository.save(match { it.id == 10L }) }
    }

    @Test
    fun `moveFile storage failure propagates`() {
        val oldKey = TempFileKey(1L, "draft.txt")
        val newKey = SubmissionFileKey(1L, 1L, 1L, "draft.txt")
        every { storage.move(oldKey, newKey) } returns Mono.just(false)

        StepVerifier.create(fileManager.moveFile(oldKey, newKey)).expectError(IllegalStateException::class.java).verify()
    }

    // endregion

    // region listFileMetadataWithPrefix

    @Test
    fun `listFileMetadataWithPrefix maps to dto`() {
        val prefix = "books/savchenko/problems/1.1.1/"
        val key = ProblemFileKey("savchenko", "1.1.1", "img.png")
        val now = Instant.now()
        val fo =
            FileObject(
                id = 1L,
                keyPath = key.toString(),
                key = key,
                type = "problem",
                ownerUserId = 0L,
                metadata = FileObjectMetadata(now, 512L, "image/png"),
            )
        every { fileObjectRepository.findAllByKeyPathStartingWith(prefix) } returns Flux.just(fo)

        StepVerifier.create(fileManager.listFileMetadataWithPrefix(prefix, "admin"))
            .assertNext { meta ->
                assertThat(meta.key).isEqualTo("img.png")
                assertThat(meta.authorName).isEqualTo("admin")
                assertThat(meta.size).isEqualTo(512L)
            }
            .verifyComplete()
    }

    // endregion
}
