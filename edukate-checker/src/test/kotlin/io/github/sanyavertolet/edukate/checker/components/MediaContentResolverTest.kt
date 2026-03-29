@file:Suppress("ReactiveStreamsUnusedPublisher")

package io.github.sanyavertolet.edukate.checker.components

import io.github.sanyavertolet.edukate.checker.services.MediaContentResolver
import io.github.sanyavertolet.edukate.checker.storage.RawKeyReadOnlyStorage
import io.mockk.every
import io.mockk.mockk
import java.nio.ByteBuffer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.core.io.buffer.DefaultDataBufferFactory
import org.springframework.http.MediaType
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.test.StepVerifier

class MediaContentResolverTest {
    private val storage = mockk<RawKeyReadOnlyStorage>()
    private val dataBufferFactory = DefaultDataBufferFactory()
    private lateinit var resolver: MediaContentResolver

    @BeforeEach
    fun setUp() {
        resolver = MediaContentResolver(storage, dataBufferFactory)
    }

    @Test
    fun `empty key list returns empty Flux`() {
        StepVerifier.create(resolver.resolveMedia(emptyList()))
            .verifyComplete()
    }

    @Test
    fun `single key fetches bytes and builds Media`() {
        val bytes = "image data".toByteArray()
        every { storage.getContent("key1") } returns Flux.just(ByteBuffer.wrap(bytes))
        every { storage.metadata("key1") } returns Mono.just(MediaType.IMAGE_JPEG)

        StepVerifier.create(resolver.resolveMedia(listOf("key1")))
            .assertNext { media ->
                assertThat(media).isNotNull
                assertThat(media.mimeType).isEqualTo(MediaType.IMAGE_JPEG)
            }
            .verifyComplete()
    }

    @Test
    fun `metadata MediaType is passed to Media`() {
        val bytes = "img".toByteArray()
        every { storage.getContent("key1") } returns Flux.just(ByteBuffer.wrap(bytes))
        every { storage.metadata("key1") } returns Mono.just(MediaType.IMAGE_PNG)

        StepVerifier.create(resolver.resolveMedia(listOf("key1")))
            .assertNext { media ->
                assertThat(media.mimeType).isEqualTo(MediaType.IMAGE_PNG)
            }
            .verifyComplete()
    }

    @Test
    fun `multiple keys return one Media per key in order`() {
        val keys = listOf("key1", "key2", "key3")
        keys.forEach { key ->
            every { storage.getContent(key) } returns Flux.just(ByteBuffer.wrap("data".toByteArray()))
            every { storage.metadata(key) } returns Mono.just(MediaType.IMAGE_JPEG)
        }

        StepVerifier.create(resolver.resolveMedia(keys))
            .expectNextCount(3)
            .verifyComplete()
    }

    @Test
    fun `S3 error propagates`() {
        every { storage.getContent("key1") } returns Flux.error(RuntimeException("S3 unavailable"))
        every { storage.metadata("key1") } returns Mono.just(MediaType.IMAGE_JPEG)

        StepVerifier.create(resolver.resolveMedia(listOf("key1")))
            .expectError(RuntimeException::class.java)
            .verify()
    }
}
