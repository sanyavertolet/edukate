package io.github.sanyavertolet.edukate.backend.services;

import io.github.sanyavertolet.edukate.backend.dtos.ProblemDto;
import io.github.sanyavertolet.edukate.backend.dtos.ProblemMetadata;
import io.github.sanyavertolet.edukate.backend.entities.Problem;
import io.github.sanyavertolet.edukate.backend.entities.files.ProblemFileKey;
import io.github.sanyavertolet.edukate.backend.repositories.ProblemRepository;
import io.github.sanyavertolet.edukate.backend.services.files.BaseFileService;
import io.github.sanyavertolet.edukate.backend.utils.Sorts;
import io.github.sanyavertolet.edukate.common.utils.AuthUtils;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProblemService {
    private final ProblemRepository problemRepository;
    private final BaseFileService baseFileService;
    private final ProblemStatusDecisionManager problemStatusDecisionManager;

    public Flux<Problem> getFilteredProblems(PageRequest pageRequest) {
        return problemRepository.findAll(pageRequest.withSort(Sorts.semVerSort()));
    }

    public Mono<Problem> findProblemById(String id) {
        return problemRepository.findById(id);
    }

    public Flux<Problem> findProblemsByIds(List<String> problemIds) {
        return problemRepository.findProblemsByIdIn(problemIds);
    }

    public Mono<Problem> updateProblem(Problem problem) {
        return problemRepository.save(problem);
    }

    public Flux<Problem> updateProblemBatch(List<Problem> problems) {
        return Flux.fromIterable(problems).flatMap(problemRepository::save);
    }

    public Mono<Long> countProblems() {
        return problemRepository.count();
    }

    public Mono<Boolean> deleteProblemById(@NonNull String id) {
        return problemRepository.deleteById(id).thenReturn(true).onErrorReturn(false);
    }

    public Flux<String> getProblemIdsByPrefix(@NonNull String prefix, int limit) {
        return problemRepository.findProblemsByIdStartingWith(prefix, Pageable.ofSize(limit))
                .map(Problem::getId);
    }

    public Mono<String> getRandomUnsolvedProblemId(@Nullable Authentication authentication) {
        return AuthUtils.monoId(authentication)
                .flatMap(problemRepository::findRandomUnsolvedProblemId)
                .switchIfEmpty(problemRepository.findRandomProblemId());
    }

    public Flux<String> problemImageDownloadUrls(@NonNull String problemId, @NonNull List<String> images) {
        return Flux.fromIterable(images)
                .map(fileName -> ProblemFileKey.of(problemId, fileName))
                .flatMap(baseFileService::getDownloadUrlOrEmpty);
    }

    public Mono<ProblemDto> prepareDto(@NonNull Problem problem, Authentication authentication) {
        return Mono.fromCallable(problem::toProblemDto)
                .flatMap(dto ->
                        problemStatusDecisionManager.getStatus(problem.getId(), authentication)
                                .map(dto::withStatus)
                )
                .flatMap(this::updateImagesInDto);
    }

    public Mono<ProblemMetadata> prepareMetadata(@NonNull Problem problem, Authentication authentication) {
        return Mono.fromCallable(problem::toProblemMetadata)
                .flatMap(metadata ->
                        problemStatusDecisionManager.getStatus(problem.getId(), authentication)
                                .map(metadata::withStatus)
                );
    }

    private Mono<ProblemDto> updateImagesInDto(@NonNull ProblemDto problemDto) {
        return problemImageDownloadUrls(problemDto.getId(), problemDto.getImages()).collectList()
                .map(problemDto::withImages);
    }
}
