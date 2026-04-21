package io.github.sanyavertolet.edukate.backend.controllers.internal

import io.github.sanyavertolet.edukate.backend.entities.Book
import io.github.sanyavertolet.edukate.backend.services.BookService
import io.swagger.v3.oas.annotations.Hidden
import io.swagger.v3.oas.annotations.security.SecurityRequirements
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@Hidden
@RestController
@SecurityRequirements
@RequestMapping("/internal/books")
class BookInternalController(private val bookService: BookService) {
    @PostMapping fun postBook(@RequestBody book: Book): Mono<Book> = bookService.save(book)
}
