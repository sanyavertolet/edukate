package io.github.sanyavertolet.edukate.backend.services.files

import io.github.sanyavertolet.edukate.backend.dtos.FileMetadata
import io.github.sanyavertolet.edukate.backend.entities.files.FileObject
import io.github.sanyavertolet.edukate.backend.entities.files.FileObjectMetadata
import io.github.sanyavertolet.edukate.backend.repositories.FileObjectRepository
import io.github.sanyavertolet.edukate.backend.storage.FileKeyStorage
import io.github.sanyavertolet.edukate.storage.keys.FileKey
import java.nio.ByteBuffer
import java.time.Duration
import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toFlux
import reactor.kotlin.core.publisher.toMono

@Service
class FileManager(private val fileObjectRepository: FileObjectRepository, private val storage: FileKeyStorage) {
    @Suppress("unused")
    @Transactional(readOnly = true)
    fun getFileObject(key: FileKey): Mono<FileObject> =
        fileObjectRepository.findByKeyPath(key.toString()).timeout(DEFAULT_TIMEOUT).doOnError { e ->
            log.error("getFile failed for key={}", key, e)
        }

    fun getFileContent(key: FileKey): Flux<ByteBuffer> =
        storage
            .getContent(key)
            .timeout(DEFAULT_TIMEOUT)
            .doOnSubscribe { log.debug("Downloading content: key={}", key) }
            .doOnError { e -> log.error("download failed for key={}", key, e) }

    @Cacheable(cacheNames = ["presigned-urls"], key = "#key.toString()")
    fun getPresignedUrl(key: FileKey): Mono<String> =
        storage.generatePresignedUrl(key).timeout(DEFAULT_TIMEOUT).doOnError { e ->
            log.error("presign failed for key={}", key, e)
        }

    @Transactional
    fun uploadFile(key: FileKey, contentType: MediaType, content: Flux<ByteBuffer>): Mono<FileKey> =
        storage
            .upload(key, contentType.toString(), content)
            .flatMap { k ->
                storage.metadata(k).flatMap { meta -> saveOrUpdateByKeyPath(k.toString(), k, meta) }.thenReturn(k)
            }
            .timeout(DEFAULT_TIMEOUT)
            .doOnSubscribe { log.debug("Uploading: key={}", key) }
            .doOnSuccess { k -> log.debug("Uploaded: key={}", k) }
            .doOnError { e -> log.error("upload failed for key={}", key, e) }

    @CacheEvict(cacheNames = ["presigned-urls"], key = "#key.toString()")
    @Transactional
    fun deleteFile(key: FileKey): Mono<Boolean> =
        storage
            .delete(key)
            .onErrorResume { e ->
                log.warn("storage delete error for key={}, will continue to DB cleanup", key, e)
                false.toMono()
            }
            .flatMap { storageDeleted ->
                fileObjectRepository.deleteByKeyPath(key.toString()).defaultIfEmpty(0L).map { dbDeletedCount ->
                    val dbDeleted = dbDeletedCount > 0
                    when {
                        storageDeleted && dbDeleted -> log.trace("Deleted storage and DB record: key={}", key)
                        storageDeleted -> log.warn("Deleted storage but no DB record found: key={}", key)
                        dbDeleted -> log.warn("DB record deleted but storage missing: key={}", key)
                        else -> log.trace("Nothing to delete (idempotent no-op): key={}", key)
                    }
                    storageDeleted || dbDeleted
                }
            }
            .timeout(DEFAULT_TIMEOUT)
            .doOnError { e -> log.error("delete failed for key={}", key, e) }

    fun doesFileExist(key: FileKey): Mono<Boolean> =
        storage
            .metadata(key)
            .timeout(DEFAULT_TIMEOUT)
            .doOnError { e -> log.error("exists check failed for key={}", key, e) }
            .map { true }
            .defaultIfEmpty(false)

    fun doFilesExist(keys: List<FileKey>): Mono<Boolean> =
        keys
            .toFlux()
            .flatMapSequential({ doesFileExist(it) }, MAX_CONCURRENCY)
            .all { it }
            .timeout(DEFAULT_TIMEOUT)
            .doOnError { e -> log.error("batch exists check failed", e) }

    @CacheEvict(cacheNames = ["presigned-urls"], key = "#oldKey.toString()")
    @Transactional
    fun moveFile(oldKey: FileKey, newKey: FileKey): Mono<FileKey> =
        storage
            .move(oldKey, newKey)
            .flatMap { success ->
                if (success) {
                    storage
                        .metadata(newKey)
                        .flatMap { meta -> saveOrUpdateByKeyPath(oldKey.toString(), newKey, meta) }
                        .thenReturn(newKey)
                } else {
                    IllegalStateException("Move failed: $oldKey -> $newKey").toMono()
                }
            }
            .timeout(DEFAULT_TIMEOUT)
            .doOnSubscribe { log.debug("Moving: {} -> {}", oldKey, newKey) }
            .doOnSuccess { k -> log.debug("Moved: {} -> {}", oldKey, k) }
            .doOnError { e -> log.error("move failed: {} -> {}", oldKey, newKey, e) }

    @Transactional(readOnly = true)
    fun listFileMetadataWithPrefix(prefix: String, authorName: String): Flux<FileMetadata> =
        fileObjectRepository
            .findAllByKeyPathStartingWith(prefix)
            .map { fo -> FileMetadata.of(fo.key.fileName, authorName, fo.metadata.lastModified, fo.metadata.contentLength) }
            .timeout(DEFAULT_TIMEOUT)
            .doOnError { e -> log.error("list metadata by prefix failed: prefix={} author={}", prefix, authorName, e) }

    private fun saveOrUpdateByKeyPath(
        lookupKeyPath: String,
        newKey: FileKey,
        metadata: FileObjectMetadata,
    ): Mono<FileObject> {
        val newPath = newKey.toString()
        val type = newKey.type()
        val ownerUserId = newKey.owner() ?: 0L

        return fileObjectRepository
            .findByKeyPath(lookupKeyPath)
            .flatMap { existing ->
                val updated = existing.withStorageState(newPath, newKey, type, ownerUserId, metadata)
                fileObjectRepository.save(updated).doOnSuccess { saved ->
                    log.trace("Updated file object: lookupKeyPath={} -> key={}", saved?.keyPath, saved?.key)
                }
            }
            .switchIfEmpty(
                fileObjectRepository
                    .findByKeyPath(newPath)
                    .flatMap { existing ->
                        val updated = existing.withStorageState(newPath, newKey, type, ownerUserId, metadata)
                        fileObjectRepository.save(updated).doOnSuccess { saved ->
                            log.debug("Re-used existing file object: key={}", saved?.key)
                        }
                    }
                    .switchIfEmpty(
                        fileObjectRepository
                            .save(FileObject.fromStorageState(newPath, newKey, type, ownerUserId, metadata))
                            .doOnSuccess { created -> log.debug("Created file object: key={}", created?.key) }
                    )
            )
            .timeout(DEFAULT_TIMEOUT)
            .doOnError { e -> log.error("saveOrUpdateByKeyPath failed: lookup={} newKey={}", lookupKeyPath, newKey, e) }
    }

    companion object {
        private const val MAX_CONCURRENCY = 8
        private val DEFAULT_TIMEOUT: Duration = Duration.ofSeconds(30)
        private val log = LoggerFactory.getLogger(FileManager::class.java)
    }
}
