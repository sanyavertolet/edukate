package io.github.sanyavertolet.edukate.storage

import java.nio.ByteBuffer
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface ReadOnlyStorage<Key : Any, Metadata : Any> {
    fun metadata(key: Key): Mono<Metadata>

    fun getContent(key: Key): Flux<ByteBuffer>

    fun getContentWithMetadata(key: Key): Mono<ContentWithMetadata<Metadata>>

    fun generatePresignedUrl(key: Key): Mono<String>

    fun prefixed(rawKeyPrefix: String): Flux<Key>
}
