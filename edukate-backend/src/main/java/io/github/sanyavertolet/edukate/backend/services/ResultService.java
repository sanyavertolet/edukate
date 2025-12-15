package io.github.sanyavertolet.edukate.backend.services;

import io.github.sanyavertolet.edukate.backend.dtos.Result;
import io.github.sanyavertolet.edukate.backend.entities.Problem;
import io.github.sanyavertolet.edukate.backend.services.files.FileManager;
import io.github.sanyavertolet.edukate.storage.keys.ResultFileKey;
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
    private final FileManager fileManager;

    public Mono<String> updateResult(Result result) {
        return problemRepository.findById(result.id())
                .map(problem -> problem.withResult(result))
                .flatMap(problemRepository::save)
                .map(Problem::getId);
    }

    public Flux<String> updateResultBatch(Flux<Result> results) {
        return results.flatMap(result ->
            problemRepository
                    .findById(result.id())
                    .map(problem -> problem.withResult(result))
                    .flatMap(problemRepository::save)
                    .map(Problem::getId));
    }

    public Mono<Result> findResultById(String id) {
        return problemRepository.findById(id).map(Problem::getResult).flatMap(this::updateImagesInResult);
    }

    private Mono<Result> updateImagesInResult(Result result) {
        return Flux.fromIterable(result.images())
                .map(fileName -> ResultFileKey.of(result.id(), fileName))
                .flatMap(fileManager::getPresignedUrl)
                .collectList()
                .zipWith(Mono.justOrEmpty(result))
                .map(tuple -> {
                    List<String> urls = tuple.getT1();
                    return tuple.getT2().withImages(urls);
                });
    }
}
