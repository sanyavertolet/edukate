package io.github.sanyavertolet.edukate.backend.services

import io.github.sanyavertolet.edukate.backend.entities.Book
import io.github.sanyavertolet.edukate.backend.repositories.BookRepository
import org.springframework.cache.annotation.CacheConfig
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Service
@CacheConfig(cacheNames = ["books"])
class BookService(private val bookRepository: BookRepository) {
    @Cacheable(key = "'all'") fun findAll(): Flux<Book> = bookRepository.findAll()

    @Cacheable(key = "#id") fun findById(id: Long): Mono<Book> = bookRepository.findById(id)

    @Cacheable(key = "#slug") fun findBySlug(slug: String): Mono<Book> = bookRepository.findBySlug(slug)

    @CacheEvict(allEntries = true) fun save(book: Book): Mono<Book> = bookRepository.save(book)
}
