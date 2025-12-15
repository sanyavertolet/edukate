package io.github.sanyavertolet.edukate.storage;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;

public interface ReadOnlyStorage<Key, Metadata> {
    Mono<Metadata> metadata(Key key);

    Flux<ByteBuffer> getContent(Key key);

    Mono<String> generatePresignedUrl(Key key);

    @SuppressWarnings("unused")
    Flux<Key> prefixed(String rawKeyPrefix);
}

