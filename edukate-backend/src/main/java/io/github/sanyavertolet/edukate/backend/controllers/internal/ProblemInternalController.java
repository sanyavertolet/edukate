package io.github.sanyavertolet.edukate.backend.controllers.internal;

import io.github.sanyavertolet.edukate.backend.entities.Problem;
import io.github.sanyavertolet.edukate.backend.services.ProblemService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/internal/problems")
@RequiredArgsConstructor
public class ProblemInternalController {
    private final ProblemService problemService;

    @PostMapping
    public Mono<Problem> postProblem(@RequestBody Problem problem) {
        return problemService.updateProblem(problem);
    }

    @DeleteMapping("/{id}")
    public Mono<Boolean> deleteProblem(@PathVariable String id) {
        return problemService.deleteProblemById(id);
    }
}
