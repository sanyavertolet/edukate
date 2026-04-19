package io.github.sanyavertolet.edukate.backend.controllers

import io.github.sanyavertolet.edukate.backend.dtos.BookDto
import io.github.sanyavertolet.edukate.backend.dtos.BookSummaryDto
import io.github.sanyavertolet.edukate.backend.services.BookService
import io.github.sanyavertolet.edukate.common.utils.orNotFound
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.Parameters
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirements
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.constraints.NotBlank
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@RestController
@Validated
@SecurityRequirements
@RequestMapping("/api/v1/books")
@Tag(name = "Books", description = "API for retrieving problem books")
class BookController(private val bookService: BookService) {
    @GetMapping
    @Operation(summary = "Get all books", description = "Retrieves a list of all available books")
    @ApiResponses(value = [ApiResponse(responseCode = "200", description = "Successfully retrieved book list")])
    fun getBooks(): Flux<BookSummaryDto> = bookService.findAll().map { BookSummaryDto(it.slug, it.subject, it.title) }

    @GetMapping("/{slug}")
    @Operation(summary = "Get book by slug", description = "Retrieves a specific book by its slug")
    @ApiResponses(
        value =
            [
                ApiResponse(responseCode = "200", description = "Successfully retrieved book"),
                ApiResponse(responseCode = "404", description = "Book not found"),
            ]
    )
    @Parameters(value = [Parameter(name = "slug", description = "Book slug", `in` = ParameterIn.PATH, required = true)])
    fun getBookBySlug(@PathVariable @NotBlank slug: String): Mono<BookDto> =
        bookService
            .findBySlug(slug)
            .map { BookDto(it.slug, it.subject, it.title, it.citation, it.description) }
            .orNotFound("Book with slug $slug not found")
}
