package io.github.sanyavertolet.edukate.backend.controllers;

import io.github.sanyavertolet.edukate.backend.dtos.ProblemDto;
import io.github.sanyavertolet.edukate.backend.dtos.ProblemMetadata;
import io.github.sanyavertolet.edukate.backend.entities.Problem;
import io.github.sanyavertolet.edukate.backend.services.ProblemService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/problems")
@RequiredArgsConstructor
public class ProblemController {
    private final ProblemService problemService;

    @GetMapping
    public Flux<ProblemMetadata> getProblemList() {
        return problemService.getAllProblems().map(Problem::toProblemMetadata);
    }

    @GetMapping("/{id}")
    public Mono<ProblemDto> getProblem(@PathVariable(name = "id") String id) {
        return problemService.getProblemById(id).map(Problem::toProblemDto);
    }
}
