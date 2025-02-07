package io.github.sanyavertolet.edukate.backend.services;

import io.github.sanyavertolet.edukate.backend.entities.Problem;
import io.github.sanyavertolet.edukate.backend.repositories.ProblemRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class ProblemService {

    private final ProblemRepository problemRepository;

    public ProblemService(ProblemRepository problemRepository) {
        this.problemRepository = problemRepository;
    }

    public Flux<Problem> getAllProblems() {
        return problemRepository.findAll();
    }

    public Mono<Problem> getProblemById(String id) {
        return problemRepository.findById(id);
    }

    public Mono<Problem> updateProblem(Problem problem) {
        return problemRepository.save(problem);
    }

    public Mono<Void> deleteProblemById(String id) {
        return problemRepository.deleteById(id);
    }
}