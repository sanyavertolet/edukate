package io.github.sanyavertolet.edukate.backend.controllers;

import io.github.sanyavertolet.edukate.backend.entities.Problem;
import io.github.sanyavertolet.edukate.backend.services.ProblemService;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/problems")
public class ProblemController {

    private final ProblemService problemService;

    public ProblemController(final ProblemService problemService) {
        this.problemService = problemService;
    }

    @GetMapping
    public Flux<String> getProblemList() {
        return problemService.getAllProblems().map(Problem::getId);
    }

    @GetMapping("/{id}")
    public Mono<Problem> getProblem(@PathVariable(name = "id") String id) {
        return problemService.getProblemById(id);
    }
}
