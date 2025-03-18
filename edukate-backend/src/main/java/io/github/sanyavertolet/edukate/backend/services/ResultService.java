package io.github.sanyavertolet.edukate.backend.services;

import io.github.sanyavertolet.edukate.backend.dtos.Result;
import io.github.sanyavertolet.edukate.backend.entities.Problem;
import io.github.sanyavertolet.edukate.backend.repositories.ProblemRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@AllArgsConstructor
public class ResultService {
    private final ProblemRepository problemRepository;
    private final FileService fileService;

    public Mono<String> updateResult(Result result) {
        return problemRepository.findById(result.id())
                .map(problem -> problem.applyResult(result))
                .flatMap(problemRepository::save)
                .map(Problem::getId);
    }

    public Flux<String> updateResultBatch(Flux<Result> results) {
        return results.flatMap(result ->
            problemRepository
                    .findById(result.id())
                    .map(problem -> problem.applyResult(result))
                    .flatMap(problemRepository::save)
                    .map(Problem::getId));
    }

    public Mono<Result> updateImagesInResult(Result result) {
        return Flux.fromIterable(result.images())
                .flatMap(fileService::getDownloadUrlOrEmpty)
                .collectList()
                .zipWith(Mono.justOrEmpty(result))
                .map(tuple -> {
                    List<String> urls = tuple.getT1();
                    return tuple.getT2().withImages(urls);
                });
    }

    public Mono<Result> findResultById(String id) {
        return problemRepository.findById(id).map(Problem::getResult);
    }
}
