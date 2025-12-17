package io.github.sanyavertolet.edukate.checker.services;

import io.github.sanyavertolet.edukate.checker.storage.RawKeyReadOnlyStorage;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.content.Media;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;
import java.util.List;

@Slf4j
@Component
@AllArgsConstructor
public class MediaContentResolver {
    private final RawKeyReadOnlyStorage storage;
    private final DataBufferFactory dataBufferFactory;

    public Flux<Media> resolveMedia(List<String> rawKeys) {
        return Flux.fromIterable(rawKeys).flatMapSequential(this::resolveMedia);
    }

    // fixme: storage should return metadata from ReadOnlyStorage#getContent
    private Mono<Media> resolveMedia(String rawKey) {
        return toByteArray(storage.getContent(rawKey))
                .zipWith(storage.metadata(rawKey), this::mediaWithByteArray);
    }

    private Mono<byte[]> toByteArray(Flux<ByteBuffer> in) {
        return DataBufferUtils.join(in.map(dataBufferFactory::wrap))
                .map(db -> {
                    byte[] bytes = new byte[db.readableByteCount()];
                    db.read(bytes);
                    DataBufferUtils.release(db);
                    return bytes;
                });
    }

    private Media mediaWithByteArray(byte[] data, MediaType mediaType) {
        return Media.builder().mimeType(mediaType).data(data).build();
    }
}
