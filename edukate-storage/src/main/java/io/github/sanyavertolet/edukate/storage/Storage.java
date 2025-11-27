package io.github.sanyavertolet.edukate.storage;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;
import java.util.Collection;

public interface Storage<Key> extends ReadOnlyStorage<Key> {
    default Mono<Key> overwrite(Key key, Flux<ByteBuffer> content) {
        return delete(key).flatMap((_) -> upload(key, content));
    }

    default Mono<Key> overwrite(Key key, Long contentLength, Flux<ByteBuffer> content) {
        return delete(key).flatMap((_) -> upload(key, contentLength, content));
    }

    Mono<Boolean> delete(Key key);

    Mono<Boolean> deleteAll(Collection<Key> keys);

    Mono<Key> upload(Key key, Flux<ByteBuffer> content);

    Mono<Key> upload(Key key, long contentLength, Flux<ByteBuffer> content);

    Mono<Boolean> move(Key source, Key target);
}
