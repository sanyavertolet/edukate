package io.github.sanyavertolet.edukate.backend.services;

import io.github.sanyavertolet.edukate.backend.storage.Storage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FileService {
    private final Storage<String> storage;

    public Flux<ByteBuffer> getFile(String key) {
        return storage.download(key);
    }

    public Mono<String> getDownloadUrlOrEmpty(String key) {
        return Mono.justOrEmpty(key).filterWhen(storage::doesExist).flatMap(storage::getDownloadUrl);
    }

    public Mono<String> uploadFile(String key, Flux<ByteBuffer> content) {
        return storage.upload(key, content);
    }

    public Mono<Boolean> deleteFile(String key) {
        return storage.delete(key);
    }

    public Flux<String> listFiles() {
        return storage.list();
    }

    public Mono<Boolean> doesFileExist(String key) {
        return storage.doesExist(key);
    }

    public Mono<Boolean> doFilesExist(List<String> keys) {
        return Flux.fromIterable(keys).map(this::doesFileExist).count().map(count -> count == keys.size());
    }

    public Mono<String> moveFile(String oldKey, String newKey) {
        return Mono.justOrEmpty(oldKey).flatMap(key -> storage.move(key, newKey)).map(_ -> newKey);
    }
}
