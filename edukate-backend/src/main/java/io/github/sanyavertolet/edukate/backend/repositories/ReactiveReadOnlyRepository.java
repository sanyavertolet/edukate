package io.github.sanyavertolet.edukate.backend.repositories;

import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.Repository;
import reactor.core.publisher.Mono;

@NoRepositoryBean
public interface ReactiveReadOnlyRepository<T, ID> extends Repository<T, ID> {
    Mono<T> findById(ID id);
}
