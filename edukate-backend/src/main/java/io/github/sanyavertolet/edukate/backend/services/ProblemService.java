package io.github.sanyavertolet.edukate.backend.services;

import io.github.sanyavertolet.edukate.backend.dtos.ProblemDto;
import io.github.sanyavertolet.edukate.backend.dtos.ProblemMetadata;
import io.github.sanyavertolet.edukate.backend.entities.Problem;
import io.github.sanyavertolet.edukate.backend.repositories.ProblemRepository;
import io.github.sanyavertolet.edukate.backend.utils.Sorts;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProblemService {
    private final ProblemRepository problemRepository;
    private final FileService fileService;

    public Flux<Problem> getFilteredProblems(PageRequest pageRequest) {
        return problemRepository.findAll(pageRequest.withSort(Sorts.semVerSort()));
    }

    public Mono<Problem> findProblemById(String id) {
        return problemRepository.findById(id);
    }

    public Mono<List<ProblemMetadata>> findProblemListByIds(List<String> problemIds) {
        return problemRepository.findProblemsByIdIn(problemIds)
                .map(Problem::toProblemMetadata)
                .collectList();
    }

    public Mono<Problem> updateProblem(Problem problem) {
        return problemRepository.save(problem);
    }

    public Flux<Problem> updateProblemBatch(Flux<Problem> problems) {
        return problems.flatMap(problemRepository::save);
    }

    public Mono<Long> countProblems() {
        return problemRepository.count();
    }

    public Mono<Boolean> deleteProblemById(String id) {
        return problemRepository.deleteById(id).thenReturn(true).onErrorReturn(false);
    }

    public Mono<ProblemDto> updateImagesInDto(ProblemDto problemDto) {
        return Flux.fromIterable(problemDto.getImages())
                .flatMap(fileService::getDownloadUrlOrEmpty)
                .collectList()
                .zipWith(Mono.justOrEmpty(problemDto))
                .map(tuple -> {
                    List<String> urls = tuple.getT1();
                    ProblemDto dto = tuple.getT2();
                    dto.setImages(urls);
                    return dto;
                });
    }

    public Flux<String> getProblemIdsByPrefix(String prefix, int limit) {
        return problemRepository.findProblemsByIdStartingWith(prefix, Pageable.ofSize(limit))
                .map(Problem::getId);
    }

    public Mono<Long> updateMissingInternalIndices() {
        return problemRepository.findProblemsWithMissingIndices()
                .flatMap(problemRepository::save)
                .count();
    }
}
