package io.github.sanyavertolet.edukate.backend.repositories;

import io.github.sanyavertolet.edukate.backend.entities.files.FileObject;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface FileObjectRepository extends ReactiveMongoRepository<FileObject, String> {

    Mono<FileObject> findByKeyPath(String keyPath);

    Flux<FileObject> findAllByKeyPathStartingWith(String prefix);

    Mono<Long> deleteByKeyPath(String keyPath);
}
