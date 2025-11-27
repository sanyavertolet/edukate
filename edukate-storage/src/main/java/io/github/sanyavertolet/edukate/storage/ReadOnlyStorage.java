package io.github.sanyavertolet.edukate.storage;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;
import java.time.Instant;

public interface ReadOnlyStorage<Key> {
    default boolean isInitDone() {
        return true;
    }

    Flux<Key> prefixedList(String prefix);

    Mono<Boolean> doesExist(Key key);

    Mono<Long> contentLength(Key key);

    Mono<Instant> lastModified(Key key);

    Flux<ByteBuffer> download(Key key);

    Mono<String> getDownloadUrl(Key key);
}
