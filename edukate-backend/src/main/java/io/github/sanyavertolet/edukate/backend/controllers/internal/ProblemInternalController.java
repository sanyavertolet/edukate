package io.github.sanyavertolet.edukate.backend.controllers.internal;

import io.github.sanyavertolet.edukate.backend.entities.Problem;
import io.github.sanyavertolet.edukate.backend.services.ProblemService;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/internal/problems")
public class ProblemInternalController {

    private final ProblemService problemService;

    public ProblemInternalController(final ProblemService problemService) {
        this.problemService = problemService;
    }

    @PostMapping
    public Mono<Problem> getProblem(@RequestBody Problem problem) {
        return problemService.updateProblem(problem);
    }
}
