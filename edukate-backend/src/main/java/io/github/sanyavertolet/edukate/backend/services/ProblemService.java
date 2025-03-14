package io.github.sanyavertolet.edukate.backend.services;

import io.github.sanyavertolet.edukate.backend.entities.Problem;
import io.github.sanyavertolet.edukate.backend.repositories.ProblemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class ProblemService {
    private final ProblemRepository problemRepository;

    public Flux<Problem> getAllProblems() {
        return problemRepository.findAll(Sort.by(Sort.Direction.ASC, "id"));
    }

    public Mono<Problem> getProblemById(String id) {
        return problemRepository.findById(id);
    }

    public Mono<Problem> updateProblem(Problem problem) {
        return problemRepository.save(problem);
    }

    public Flux<Problem> updateProblemBatch(Flux<Problem> problems) {
        return problems.flatMap(problemRepository::save);
    }

    public Mono<Boolean> deleteProblemById(String id) {
        return problemRepository.deleteById(id).thenReturn(true).onErrorReturn(false);
    }
}
