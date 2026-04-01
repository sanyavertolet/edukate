package io.github.sanyavertolet.edukate.backend.repositories

import io.github.sanyavertolet.edukate.backend.entities.UserProblemStatus
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
interface UserProblemStatusRepository : ReactiveReadOnlyRepository<UserProblemStatus, String> {
    fun findByUserIdAndProblemId(userId: String, problemId: String): Mono<UserProblemStatus>
}
