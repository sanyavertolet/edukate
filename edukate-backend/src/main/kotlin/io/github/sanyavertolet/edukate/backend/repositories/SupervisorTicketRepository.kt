package io.github.sanyavertolet.edukate.backend.repositories

import io.github.sanyavertolet.edukate.backend.entities.SupervisorTicket
import org.springframework.data.domain.Pageable
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
interface SupervisorTicketRepository : ReactiveCrudRepository<SupervisorTicket, Long> {
    @Query(
        """
        SELECT st.* FROM supervisor_tickets st
        WHERE st.supervisor_id = :supervisorId
        ORDER BY st.created_at DESC
        """
    )
    fun findBySupervisorId(supervisorId: Long, pageable: Pageable): Flux<SupervisorTicket>

    @Query(
        """
        SELECT st.* FROM supervisor_tickets st
        JOIN problem_sets ps ON ps.id = st.problem_set_id
        WHERE st.supervisor_id = :supervisorId AND ps.share_code = :problemSetCode
        ORDER BY st.created_at DESC
        """
    )
    fun findBySupervisorIdAndProblemSetCode(
        supervisorId: Long,
        problemSetCode: String,
        pageable: Pageable,
    ): Flux<SupervisorTicket>

    @Query("SELECT COUNT(*) FROM supervisor_tickets WHERE supervisor_id = :supervisorId")
    fun countBySupervisorId(supervisorId: Long): Mono<Long>

    @Query(
        """
        SELECT COUNT(*) FROM supervisor_tickets st
        JOIN problem_sets ps ON ps.id = st.problem_set_id
        WHERE st.supervisor_id = :supervisorId AND ps.share_code = :problemSetCode
        """
    )
    fun countBySupervisorIdAndProblemSetCode(supervisorId: Long, problemSetCode: String): Mono<Long>
}
