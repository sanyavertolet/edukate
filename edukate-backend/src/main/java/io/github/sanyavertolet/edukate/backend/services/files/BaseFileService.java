package io.github.sanyavertolet.edukate.backend.services.files;

import io.github.sanyavertolet.edukate.backend.dtos.FileMetadata;
import io.github.sanyavertolet.edukate.backend.entities.files.FileKey;
import io.github.sanyavertolet.edukate.backend.storage.Storage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BaseFileService {
    private final Storage<FileKey> storage;

    public Flux<ByteBuffer> getFile(FileKey key) {
        return storage.download(key);
    }

    public Mono<String> getDownloadUrlOrEmpty(FileKey key) {
        return Mono.justOrEmpty(key).filterWhen(storage::doesExist).flatMap(storage::getDownloadUrl);
    }

    public Mono<FileKey> uploadFile(FileKey key, Flux<ByteBuffer> content) {
        return storage.upload(key, content);
    }

    public Mono<Boolean> deleteFile(FileKey key) {
        return storage.delete(key);
    }

    public Mono<Boolean> doesFileExist(FileKey key) {
        return storage.doesExist(key);
    }

    public Mono<Boolean> doFilesExist(List<FileKey> keys) {
        return Flux.fromIterable(keys).flatMap(this::doesFileExist).all(Boolean::booleanValue);
    }

    public Mono<FileKey> moveFile(FileKey oldKey, FileKey newKey) {
        return Mono.justOrEmpty(oldKey)
                .flatMap(key -> storage.move(key, newKey))
                .flatMap(success -> success
                        ? Mono.just(newKey)
                        : Mono.error(new IllegalStateException("Move failed: " + oldKey + " -> " + newKey))
                );
    }

    public Flux<FileKey> listFilesWithPrefix(String prefix) {
        return Mono.justOrEmpty(prefix).flatMapMany(storage::prefixedList);
    }

    public Flux<FileMetadata> listFileMetadataWithPrefix(String prefix, String authorName) {
        return listFilesWithPrefix(prefix)
                .flatMap(key -> Mono.zip(
                        Mono.just(key),
                        storage.lastModified(key),
                        storage.contentLength(key)
                ))
                .map(tuple -> FileMetadata.of(
                        tuple.getT1().getFileName(),
                        authorName,
                        LocalDateTime.ofInstant(tuple.getT2(), ZoneId.systemDefault()),
                        tuple.getT3()
                ));
    }
}
