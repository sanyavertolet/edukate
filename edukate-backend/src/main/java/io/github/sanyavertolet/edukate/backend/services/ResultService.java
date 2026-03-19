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
/*
 * todo: refactor Result entity in order to split the persistent result with human readable result (with correct pics)
 */
public class ResultService {
    private final ProblemRepository problemRepository;
    private final FileManager fileManager;

    public Mono<String> updateResult(Result result) {
        return problemRepository.findById(result.getId())
                .map(problem -> problem.withResult(result))
                .flatMap(problemRepository::save)
                .map(Problem::getId);
    }

    public Flux<String> updateResultBatch(Flux<Result> results) {
        return results.flatMap(result -> problemRepository
                .findById(result.getId())
                .map(problem -> problem.withResult(result))
                .flatMap(problemRepository::save)
                .map(Problem::getId));
    }

    public Mono<Result> findResultById(String id) {
        return problemRepository.findById(id).mapNotNull(Problem::getResult).flatMap(this::updateImagesInResult);
    }

    private Mono<List<String>> getResultImageList(Result result) {
        return Flux.fromIterable(result.getImages())
                .map(fileName -> ResultFileKey.of(result.getId(), fileName))
                .flatMap(fileManager::getPresignedUrl)
                .collectList();
    }

    private Mono<Result> updateImagesInResult(Result result) {
        return getResultImageList(result).defaultIfEmpty(List.of()).map(result::withImages);
    }
}
