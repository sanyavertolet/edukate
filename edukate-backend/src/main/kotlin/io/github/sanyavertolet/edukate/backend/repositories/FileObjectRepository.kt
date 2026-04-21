package io.github.sanyavertolet.edukate.backend.repositories

import io.github.sanyavertolet.edukate.backend.entities.files.FileObject
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
interface FileObjectRepository : ReactiveCrudRepository<FileObject, Long> {
    fun findByKeyPath(keyPath: String): Mono<FileObject>

    fun findAllByKeyPathStartingWith(prefix: String): Flux<FileObject>

    fun deleteByKeyPath(keyPath: String): Mono<Long>
}
