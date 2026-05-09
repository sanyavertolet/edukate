package io.github.sanyavertolet.edukate.backend.controllers.internal

import io.github.sanyavertolet.edukate.backend.dtos.ingestion.BookIngestionRequest
import io.github.sanyavertolet.edukate.backend.dtos.ingestion.BookIngestionResponse
import io.github.sanyavertolet.edukate.backend.services.BookIngestionService
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
@RequestMapping("/internal/ingest")
class BookIngestionController(private val bookIngestionService: BookIngestionService) {
    @PostMapping("/book")
    fun ingestBook(@RequestBody request: BookIngestionRequest): Mono<BookIngestionResponse> =
        bookIngestionService.ingest(request)
}
