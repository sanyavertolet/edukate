package io.github.sanyavertolet.edukate.backend.controllers.internal

import io.github.sanyavertolet.edukate.backend.dtos.Result
import io.github.sanyavertolet.edukate.backend.services.ResultService
import io.swagger.v3.oas.annotations.Hidden
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Hidden
@RestController
@RequestMapping("/internal/results")
class ResultInternalController(private val resultService: ResultService) {
    @PostMapping fun postResult(@RequestBody result: Result): Mono<String> = resultService.updateResult(result)

    @PostMapping("/batch")
    fun postResultsBatch(@RequestBody results: Flux<Result>): Flux<String> = resultService.updateResultBatch(results)
}
