package io.github.sanyavertolet.edukate.checker.services

import io.github.sanyavertolet.edukate.checker.storage.RawKeyReadOnlyStorage
import java.nio.ByteBuffer
import org.springframework.ai.content.Media
import org.springframework.core.io.buffer.DataBufferUtils
import org.springframework.core.io.buffer.DefaultDataBufferFactory
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Component
class MediaContentResolver(private val storage: RawKeyReadOnlyStorage) {
    fun resolveMedia(rawKeys: List<String>): Flux<Media> = Flux.fromIterable(rawKeys).flatMapSequential(::resolveMedia)

    // fixme: storage should return metadata from ReadOnlyStorage#getContent
    private fun resolveMedia(rawKey: String): Mono<Media> =
        toByteArray(storage.getContent(rawKey)).zipWith(storage.metadata(rawKey), ::mediaWithByteArray)

    private fun toByteArray(input: Flux<ByteBuffer>): Mono<ByteArray> =
        DataBufferUtils.join(input.map(DefaultDataBufferFactory.sharedInstance::wrap)).map { db ->
            val bytes = ByteArray(db.readableByteCount())
            db.read(bytes)
            DataBufferUtils.release(db)
            bytes
        }

    private fun mediaWithByteArray(data: ByteArray, mediaType: MediaType): Media =
        Media.builder().mimeType(mediaType).data(data).build()
}
