package io.github.sanyavertolet.edukate.backend.services;

import io.github.sanyavertolet.edukate.backend.entities.Problem;
import io.github.sanyavertolet.edukate.backend.entities.UserProblemStatus;
import io.github.sanyavertolet.edukate.backend.repositories.UserProblemStatusRepository;
import io.github.sanyavertolet.edukate.common.utils.AuthUtils;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@AllArgsConstructor
public class ProblemStatusDecisionManager {
    private final UserProblemStatusRepository userProblemStatusRepository;

    public Mono<Problem.Status> getStatus(String userId, String problemId) {
        return userProblemStatusRepository.findByUserIdAndProblemId(userId, problemId)
                .flatMap(this::statusDecision)
                .defaultIfEmpty(Problem.Status.NOT_SOLVED);
    }

    public Mono<Problem.Status> getStatus(String problemId, Authentication authentication) {
        return AuthUtils.monoId(authentication)
                .flatMap(userId -> getStatus(userId, problemId))
                .defaultIfEmpty(Problem.Status.NOT_SOLVED);
    }

    /**
     * bestStatus == Submission.Status.SUCCESS  =>  Problem.Status.SOLVED
     * bestStatus == Submission.Status.FAILED   =>  Problem.Status.FAILED
     * bestStatus == Submission.Status.PENDING  =>  Problem.Status.SOLVING
     * else                                     =>  Problem.Status.NOT_SOLVED
     */
    private Mono<Problem.Status> statusDecision(UserProblemStatus userProblemStatus) {
        return Mono.justOrEmpty(userProblemStatus)
                .map(UserProblemStatus::getBestStatus)
                .map(best -> switch (best) {
                    case SUCCESS -> Problem.Status.SOLVED;
                    case FAILED -> Problem.Status.FAILED;
                    case PENDING -> Problem.Status.SOLVING;
                })
                .defaultIfEmpty(Problem.Status.NOT_SOLVED);
    }
}
