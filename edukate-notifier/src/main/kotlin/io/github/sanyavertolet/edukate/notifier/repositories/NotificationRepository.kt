package io.github.sanyavertolet.edukate.notifier.repositories

import io.github.sanyavertolet.edukate.notifier.dtos.NotificationStatistics
import io.github.sanyavertolet.edukate.notifier.entities.BaseNotification
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.repository.Aggregation
import org.springframework.data.mongodb.repository.Query
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
interface NotificationRepository : ReactiveMongoRepository<BaseNotification, String> {
    @Query("{ 'uuid': ?0 }") fun findNotificationByUuid(uuid: String): Mono<BaseNotification>

    @Query("{ 'targetUserId': ?0, 'isRead': ?1 }")
    fun findAllByTargetUserIdAndIsRead(targetUserId: Long, isRead: Boolean, pageable: Pageable): Flux<BaseNotification>

    @Query("{ 'targetUserId': ?0 }")
    fun findAllByTargetUserId(targetUserId: Long, pageable: Pageable): Flux<BaseNotification>

    @Query($$"{ 'targetUserId': ?0, 'uuid': { $in: ?1 } }")
    fun findByTargetUserIdAndUuidIn(targetUserId: Long, uuid: List<String>): Flux<BaseNotification>

    @Aggregation(
        pipeline =
            [
                $$"{ $match: { targetUserId: ?0 } }",
                ($$"{ $facet: { " +
                    $$"  unread: [{ $match: { isRead: false } }, { $count: 'count' }], " +
                    $$"  total: [{ $count: 'count' }] " +
                    "} }"),
                ($$"{ $project: { " +
                    $$"  unread: { $ifNull: [{ $arrayElemAt: ['$unread.count', 0] }, 0] }, " +
                    $$"  total: { $ifNull: [{ $arrayElemAt: ['$total.count', 0] }, 0] } " +
                    "} }"),
            ]
    )
    fun gatherStatistics(targetUserId: Long): Mono<NotificationStatistics>
}
