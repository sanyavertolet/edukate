package io.github.sanyavertolet.edukate.backend.storage;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.Collection;

public interface Storage<Key> {
    default boolean isInitDone() {
        return true;
    }

    default Mono<Key> overwrite(Key key, Flux<ByteBuffer> content) {
        return delete(key).flatMap((_) -> upload(key, content));
    }

    default Mono<Key> overwrite(Key key, Long contentLength, Flux<ByteBuffer> content) {
        return delete(key).flatMap((_) -> upload(key, contentLength, content));
    }

    Flux<Key> list();

    Mono<Boolean> doesExist(Key key);

    Mono<Long> contentLength(Key key);

    Mono<Instant> lastModified(Key key);

    Mono<Boolean> delete(Key key);

    Mono<Boolean> deleteAll(Collection<Key> keys);

    Mono<Key> upload(Key key, Flux<ByteBuffer> content);

    Mono<Key> upload(Key key, long contentLength, Flux<ByteBuffer> content);

    Flux<ByteBuffer> download(Key key);

    Mono<Boolean> move(Key source, Key target);
}
