package io.github.sanyavertolet.edukate.storage

import java.nio.ByteBuffer
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface Storage<Key : Any, Metadata> : ReadOnlyStorage<Key, Metadata> {
    fun upload(key: Key, contentType: String, content: Flux<ByteBuffer>): Mono<Key> =
        content.collectList().flatMap { buffers ->
            val totalSize = buffers.sumOf { it.remaining() }.toLong()
            upload(key, totalSize, contentType, Flux.fromIterable(buffers))
        }

    fun upload(key: Key, contentLength: Long, contentType: String, content: Flux<ByteBuffer>): Mono<Key>

    fun move(source: Key, target: Key): Mono<Boolean>

    fun delete(key: Key): Mono<Boolean>

    fun deleteAll(keys: Collection<Key>): Mono<Boolean>
}
