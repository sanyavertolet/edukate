package io.github.sanyavertolet.edukate.common.repositories;

import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@NoRepositoryBean
@SuppressWarnings("unused")
public interface ReactiveReadOnlyRepository<T, ID> extends Repository<T, ID> {
    Mono<T> findById(ID id);

    Flux<T> findAll();
}
