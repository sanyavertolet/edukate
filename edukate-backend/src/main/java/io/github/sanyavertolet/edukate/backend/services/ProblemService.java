package io.github.sanyavertolet.edukate.backend.services;

import io.github.sanyavertolet.edukate.backend.entities.Problem;
import io.github.sanyavertolet.edukate.backend.repositories.ProblemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProblemService {
    private final ProblemRepository problemRepository;

    public Flux<Problem> getFilteredProblems(PageRequest pageable) {
        return problemRepository.findAllByIsHardIn(
                List.of(true, false),
                pageable.withSort(Sort.Direction.ASC, "id")
        );
    }

    public Mono<Problem> findProblemById(String id) {
        return problemRepository.findById(id);
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
}
