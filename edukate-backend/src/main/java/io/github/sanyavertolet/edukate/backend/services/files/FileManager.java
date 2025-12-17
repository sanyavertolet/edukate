package io.github.sanyavertolet.edukate.backend.services.files;

import io.github.sanyavertolet.edukate.backend.dtos.FileMetadata;
import io.github.sanyavertolet.edukate.backend.entities.files.*;
import io.github.sanyavertolet.edukate.backend.repositories.FileObjectRepository;
import io.github.sanyavertolet.edukate.backend.storage.FileKeyStorage;
import io.github.sanyavertolet.edukate.storage.keys.*;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileManager {
    private final FileObjectRepository fileObjectRepository;
    private final FileKeyStorage storage;

    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(30);

    @SuppressWarnings("unused")
    @Transactional(readOnly = true)
    public Mono<FileObject> getFileObject(@NonNull FileKey key) {
        Objects.requireNonNull(key, "key must not be null");
        return fileObjectRepository.findByKeyPath(key.toString())
                .timeout(DEFAULT_TIMEOUT)
                .doOnError(e -> log.error("getFile failed for key={}", key, e));
    }

    public Flux<ByteBuffer> getFileContent(@NonNull FileKey key) {
        Objects.requireNonNull(key, "key must not be null");
        return storage.getContent(key)
                .timeout(DEFAULT_TIMEOUT)
                .doOnSubscribe(_ -> log.debug("Downloading content: key={}", key))
                .doOnError(e -> log.error("download failed for key={}", key, e));
    }

    public Mono<String> getPresignedUrl(@NonNull FileKey key) {
        Objects.requireNonNull(key, "key must not be null");
        return storage.generatePresignedUrl(key)
                .timeout(DEFAULT_TIMEOUT)
                .doOnError(e -> log.error("presign failed for key={}", key, e));
    }

    public Flux<FileObject> getFileObjectsByIds(List<String> ids) {
        return fileObjectRepository.findAllById(ids);
    }

    @Transactional
    public Mono<FileKey> uploadFile(@NonNull FileKey key, MediaType contentType, @NonNull Flux<ByteBuffer> content) {
        Objects.requireNonNull(key, "key must not be null");
        Objects.requireNonNull(content, "content must not be null");

        return storage.upload(key, contentType.toString(), content)
                .flatMap(k -> storage.metadata(k)
                        .flatMap(meta -> saveOrUpdateByKeyPath(k.toString(), k, meta))
                        .thenReturn(k))
                .timeout(DEFAULT_TIMEOUT)
                .doOnSubscribe(_ -> log.debug("Uploading: key={}", key))
                .doOnSuccess(k -> log.debug("Uploaded: key={}", k))
                .doOnError(e -> log.error("upload failed for key={}", key, e));
    }

    @Transactional
    public Mono<Boolean> deleteFile(@NonNull FileKey key) {
        Objects.requireNonNull(key, "key must not be null");
        final String keyPath = key.toString();

        return storage.delete(key)
                .onErrorResume(e -> {
                    log.warn("storage delete error for key={}, will continue to DB cleanup", key, e);
                    return Mono.just(false);
                })
                .flatMap(storageDeleted -> fileObjectRepository.deleteByKeyPath(keyPath)
                        .defaultIfEmpty(0L)
                        .map(dbDeletedCount -> {
                            boolean dbDeleted = dbDeletedCount != null && dbDeletedCount > 0;
                            if (storageDeleted && dbDeleted) {
                                log.trace("Deleted storage and DB record: key={}", key);
                            } else if (storageDeleted) {
                                log.warn("Deleted storage but no DB record found: key={}", key);
                            } else if (dbDeleted) {
                                log.warn("DB record deleted but storage missing: key={}", key);
                            } else {
                                log.trace("Nothing to delete (idempotent no-op): key={}", key);
                            }
                            return storageDeleted || dbDeleted;
                        }))
                .timeout(DEFAULT_TIMEOUT)
                .doOnError(e -> log.error("delete failed for key={}", key, e));
    }

    public Mono<Boolean> doesFileExist(FileKey key) {
        Objects.requireNonNull(key, "key must not be null");
        return storage.metadata(key)
                .timeout(DEFAULT_TIMEOUT)
                .doOnError(e -> log.error("exists check failed for key={}", key, e))
                .thenReturn(true)
                .defaultIfEmpty(false);
    }

    public Mono<Boolean> doFilesExist(List<FileKey> keys) {
        Objects.requireNonNull(keys, "keys must not be null");
        return Flux.fromIterable(keys)
                .flatMapSequential(this::doesFileExist, 8) // limit concurrency
                .all(Boolean::booleanValue)
                .timeout(DEFAULT_TIMEOUT)
                .doOnError(e -> log.error("batch exists check failed", e));
    }

    @Transactional
    public Mono<FileKey> moveFile(FileKey oldKey, FileKey newKey) {
        Objects.requireNonNull(oldKey, "oldKey must not be null");
        Objects.requireNonNull(newKey, "newKey must not be null");

        return storage.move(oldKey, newKey)
                .flatMap(success -> success
                        ? storage.metadata(newKey)
                        .flatMap(meta -> saveOrUpdateByKeyPath(oldKey.toString(), newKey, meta))
                        .thenReturn(newKey)
                        : Mono.error(new IllegalStateException("Move failed: " + oldKey + " -> " + newKey)))
                .timeout(DEFAULT_TIMEOUT)
                .doOnSubscribe(_ -> log.debug("Moving: {} -> {}", oldKey, newKey))
                .doOnSuccess(k -> log.debug("Moved: {} -> {}", oldKey, k))
                .doOnError(e -> log.error("move failed: {} -> {}", oldKey, newKey, e));
    }

    @Transactional(readOnly = true)
    public Flux<FileMetadata> listFileMetadataWithPrefix(String prefix, String authorName) {
        Objects.requireNonNull(prefix, "prefix must not be null");
        return fileObjectRepository.findAllByKeyPathStartingWith(prefix)
                .map(fo -> FileMetadata.of(
                        fo.getKey() != null ? fo.getKey().getFileName() : fo.getKeyPath(),
                        authorName,
                        fo.getMetadata() != null ? fo.getMetadata().getLastModified() : null,
                        fo.getMetadata() != null ? fo.getMetadata().getContentLength() : null
                ))
                .timeout(DEFAULT_TIMEOUT)
                .doOnError(e -> log.error("list metadata by prefix failed: prefix={} author={}", prefix, authorName, e));
    }

    private Mono<FileObject> saveOrUpdateByKeyPath(String lookupKeyPath, FileKey newKey, FileObjectMetadata metadata) {
        final String newPath = newKey.toString();
        final String type = FileKey.typeOf(newKey);
        final String owner = FileKey.ownerOf(newKey);

        return fileObjectRepository.findByKeyPath(lookupKeyPath)
                .flatMap(existing -> {
                    existing.setKey(newKey);
                    existing.setKeyPath(newPath);
                    existing.setType(type);
                    existing.setOwnerUserId(owner);
                    existing.setMetadata(metadata);
                    return fileObjectRepository.save(existing).doOnSuccess(updated -> log.trace(
                            "Updated file object: lookupKeyPath={} -> key={}", lookupKeyPath, updated.getKey())
                    );
                })
                .switchIfEmpty(fileObjectRepository.save(FileObject.builder()
                                .keyPath(newPath)
                                .key(newKey)
                                .type(type)
                                .ownerUserId(owner)
                                .metadata(metadata)
                                .metaVersion(1)
                                .build())
                        .doOnSuccess(created -> log.debug("Created file object: key={}", created.getKey())))
                .timeout(DEFAULT_TIMEOUT)
                .doOnError(e -> log.error("saveOrUpdateByKeyPath failed: lookup={} newKey={}", lookupKeyPath, newKey, e));
    }
}
