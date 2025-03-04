package io.github.sanyavertolet.edukate.backend.controllers;

import io.github.sanyavertolet.edukate.backend.dtos.ProblemDto;
import io.github.sanyavertolet.edukate.backend.dtos.ProblemMetadata;
import io.github.sanyavertolet.edukate.backend.entities.Problem;
import io.github.sanyavertolet.edukate.backend.services.FileService;
import io.github.sanyavertolet.edukate.backend.services.ProblemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/api/v1/problems")
@RequiredArgsConstructor
public class ProblemController {
    private final ProblemService problemService;
    private final FileService fileService;

    @GetMapping
    public Flux<ProblemMetadata> getProblemList() {
        return problemService.getAllProblems().map(Problem::toProblemMetadata);
    }

    @GetMapping("/{id}")
    public Mono<ProblemDto> getProblem(@PathVariable String id) {
        return problemService.getProblemById(id).map(Problem::toProblemDto)
                .zipWhen((problemDto) -> Flux.fromIterable(problemDto.getImages())
                        .flatMap(fileService::getDownloadUrl)
                        .collectList())
                .map(tuple -> {
                    ProblemDto dto = tuple.getT1();
                    List<String> imageUrls = tuple.getT2();
                    dto.setImages(imageUrls);
                    return dto;
                })
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Problem not found")));
    }
}
