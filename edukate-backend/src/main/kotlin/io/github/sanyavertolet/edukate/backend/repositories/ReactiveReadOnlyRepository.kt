package io.github.sanyavertolet.edukate.backend.repositories

import org.springframework.data.repository.NoRepositoryBean
import org.springframework.data.repository.Repository
import reactor.core.publisher.Mono

@NoRepositoryBean
interface ReactiveReadOnlyRepository<T : Any, ID : Any> : Repository<T, ID> {
    fun findById(id: ID): Mono<T>
}
