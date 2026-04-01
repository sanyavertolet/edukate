package io.github.sanyavertolet.edukate.backend.repositories

import io.github.sanyavertolet.edukate.backend.entities.files.FileObject
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
interface FileObjectRepository : ReactiveMongoRepository<FileObject, String> {
    fun findByKeyPath(keyPath: String): Mono<FileObject>

    fun findAllByKeyPathStartingWith(prefix: String): Flux<FileObject>

    fun deleteByKeyPath(keyPath: String): Mono<Long>
}
