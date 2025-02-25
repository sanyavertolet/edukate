package io.github.sanyavertolet.edukate.backend.controllers.internal;

import io.github.sanyavertolet.edukate.backend.services.FileService;
import io.github.sanyavertolet.edukate.backend.services.ProblemService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.nio.ByteBuffer;

@RestController
@RequestMapping("/internal/images")
@AllArgsConstructor
public class ImagesInternalController {
    private final FileService fileService;
    private final ProblemService problemService;

    @PostMapping("/upload/{id}")
    public Mono<String> uploadImage(@PathVariable String id, @RequestParam String filename, @RequestBody Flux<ByteBuffer> content) {
        return problemService.getProblemById(id)
                .switchIfEmpty(
                        Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Problem with id " + id + " not found"))
                )
                .doOnNext(_ -> fileService.uploadProblemImage(filename, content).subscribeOn(Schedulers.boundedElastic()).subscribe())
                .switchIfEmpty(
                        Mono.error(new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not upload file"))
                )
                .flatMap(problem -> {
                    problem.addImageIfNotPresent(filename);
                    return problemService.updateProblem(problem);
                })
                .map(_ -> "Successfully added " + filename + " to problem " + id);
    }

    @PutMapping("/link/{id}")
    public Mono<String> addImageKey(@PathVariable String id, @RequestParam String key) {
        return problemService.getProblemById(id)
                .switchIfEmpty(
                        Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Problem with id " + id + " not found"))
                )
                .flatMap(problem -> {
                    problem.addImageIfNotPresent(key);
                    return problemService.updateProblem(problem);
                })
                .map(_ -> "Successfully added " + key + " to problem " + id);
    }
}
