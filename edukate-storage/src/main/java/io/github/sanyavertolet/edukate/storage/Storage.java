package io.github.sanyavertolet.edukate.storage;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;
import java.util.Collection;

public interface Storage<Key, Metadata> extends ReadOnlyStorage<Key, Metadata> {

    default Mono<Key> upload(Key key, String contentType, Flux<ByteBuffer> content) {
        return content.collectList().flatMap(buffers -> {
            int totalSize = buffers.stream().mapToInt(ByteBuffer::remaining).sum();
            return upload(key, totalSize, contentType, Flux.fromIterable(buffers));
        });
    }

    Mono<Key> upload(Key key, long contentLength, String contentType, Flux<ByteBuffer> content);

    Mono<Boolean> move(Key source, Key target);

    Mono<Boolean> delete(Key key);

    @SuppressWarnings("unused")
    Mono<Boolean> deleteAll(Collection<Key> keys);
}
