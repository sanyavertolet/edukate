package io.github.sanyavertolet.edukate.backend.controllers.internal

import io.github.sanyavertolet.edukate.backend.entities.Problem
import io.github.sanyavertolet.edukate.backend.services.ProblemService
import io.swagger.v3.oas.annotations.Hidden
import io.swagger.v3.oas.annotations.security.SecurityRequirements
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Hidden
@RestController
@SecurityRequirements
@RequestMapping("/internal/problems")
class ProblemInternalController(private val problemService: ProblemService) {
    @PostMapping fun postProblem(@RequestBody problem: Problem): Mono<Problem> = problemService.updateProblem(problem)

    @PostMapping("/batch")
    fun postProblemBatch(@RequestBody problems: Flux<Problem>): Mono<Long> = problemService.updateProblemBatch(problems)

    @DeleteMapping("/{id}") fun deleteProblem(@PathVariable id: Long): Mono<Void> = problemService.deleteProblemById(id)
}
