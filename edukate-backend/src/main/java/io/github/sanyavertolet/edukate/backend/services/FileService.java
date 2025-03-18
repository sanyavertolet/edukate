package io.github.sanyavertolet.edukate.backend.services;

import io.github.sanyavertolet.edukate.backend.storage.Storage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;

@Service
@RequiredArgsConstructor
public class FileService {
    private final Storage<String> storage;

    public Flux<ByteBuffer> getFile(String key) {
        return storage.download(key);
    }

    public Mono<String> getDownloadUrlOrEmpty(String key) {
        return Mono.justOrEmpty(key)
                .filterWhen(storage::doesExist)
                .flatMap(storage::getDownloadUrl);
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
}
