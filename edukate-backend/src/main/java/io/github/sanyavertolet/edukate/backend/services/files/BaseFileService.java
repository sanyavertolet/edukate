package io.github.sanyavertolet.edukate.backend.services.files;

import io.github.sanyavertolet.edukate.backend.dtos.FileMetadata;
import io.github.sanyavertolet.edukate.backend.entities.files.*;
import io.github.sanyavertolet.edukate.backend.repositories.FileObjectRepository;
import io.github.sanyavertolet.edukate.backend.storage.Storage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuples;

import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BaseFileService {
    private final FileObjectRepository fileObjectRepository;
    private final Storage<FileKey> storage;

    public Flux<ByteBuffer> getFile(FileKey key) {
        return storage.download(key);
    }

    public Mono<String> getDownloadUrlOrEmpty(FileKey key) {
        return Mono.justOrEmpty(key).filterWhen(storage::doesExist).flatMap(storage::getDownloadUrl);
    }

    public Mono<FileKey> uploadFile(FileKey key, Flux<ByteBuffer> content) {
        return storage.upload(key, content)
                .flatMap(k -> fetchBasicMetadata(k)
                        .flatMap(meta -> saveOrUpdateByKeyPath(k.toString(), k, meta))
                        .thenReturn(k)
                );
    }

    public Mono<Boolean> deleteFile(FileKey key) {
        return storage.delete(key).flatMap(success -> success
                ? fileObjectRepository.deleteByKeyPath(key.toString())
                .doOnSuccess(cnt -> {
                    if (cnt != null && cnt > 0) {
                        log.info("Deleted file object with key path {}", key);
                    } else {
                        log.warn("Storage deleted but no DB record found for key path {}", key);
                    }
                })
                .thenReturn(true)
                : Mono.just(false)
        );
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
                        ? fetchBasicMetadata(newKey)
                            .flatMap(meta -> saveOrUpdateByKeyPath(oldKey.toString(), newKey, meta))
                            .thenReturn(newKey)
                        : Mono.error(new IllegalStateException("Move failed: " + oldKey + " -> " + newKey))
                );
    }

    public Flux<FileMetadata> listFileMetadataWithPrefix(String prefix, String authorName) {
        return fileObjectRepository.findAllByKeyPathStartingWith(prefix)
                .map(fo -> FileMetadata.of(
                        fo.getKey() != null ? fo.getKey().getFileName() : fo.getKeyPath(),
                        authorName,
                        fo.getMetadata() != null ? fo.getMetadata().getLastModified() : null,
                        fo.getMetadata() != null ? fo.getMetadata().getContentLength() : null
                ));
    }

    private static String typeOf(FileKey key) {
        if (key instanceof TempFileKey) return "tmp";
        if (key instanceof SubmissionFileKey) return "submission";
        if (key instanceof ProblemFileKey) return "problem";
        if (key instanceof ResultFileKey) return "result";
        return "base";
    }

    private static String ownerOf(FileKey key) {
        if (key instanceof TempFileKey tmp) return tmp.getUserId();
        if (key instanceof SubmissionFileKey sub) return sub.getUserId();
        return null;
    }

    private Mono<FileObjectMetadata> fetchBasicMetadata(FileKey key) {
        return Mono.zip(storage.lastModified(key), storage.contentLength(key))
                .defaultIfEmpty(Tuples.of(Instant.now(), 0L))
                .map(t -> FileObjectMetadata.builder()
                        .lastModified(t.getT1())
                        .contentLength(t.getT2())
                        .build()
                );
    }

    private Mono<FileObject> saveOrUpdateByKeyPath(String lookupKeyPath, FileKey newKey, FileObjectMetadata metadata) {
        String newPath = newKey.toString();
        String type = typeOf(newKey);
        String owner = ownerOf(newKey);
        return fileObjectRepository.findByKeyPath(lookupKeyPath)
                .flatMap(existing -> {
                    existing.setKey(newKey);
                    existing.setKeyPath(newPath);
                    existing.setType(type);
                    existing.setOwnerUserId(owner);
                    existing.setMetadata(metadata);
                    return fileObjectRepository.save(existing).doOnSuccess(updated ->
                            log.info("Updated file object with key path {}: {}", lookupKeyPath, updated.getKey()));
                })
                .switchIfEmpty(fileObjectRepository.save(
                        FileObject.builder()
                                .keyPath(newPath)
                                .key(newKey)
                                .type(type)
                                .ownerUserId(owner)
                                .metadata(metadata)
                                .metaVersion(1)
                                .build())
                        .doOnSuccess(created -> log.info("Created file object with key path {}", created.getKey()))
                );
    }
}
