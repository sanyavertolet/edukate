package io.github.sanyavertolet.edukate.notifier.repositories;

import io.github.sanyavertolet.edukate.notifier.dtos.NotificationStatistics;
import io.github.sanyavertolet.edukate.notifier.entities.BaseNotification;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Repository
public interface NotificationRepository extends ReactiveMongoRepository<BaseNotification, String> {
    Mono<BaseNotification> findNotificationByUuid(String uuid);

    Flux<BaseNotification> findAllByTargetUserNameAndIsRead(String targetUserName, Boolean isRead, Pageable pageable);

    Flux<BaseNotification> findAllByTargetUserName(String targetUserName, Pageable pageable);

    Flux<BaseNotification> findByTargetUserNameAndUuidIn(String targetUserName, List<String> uuid);

    @Aggregation(pipeline = {
            "{ $match: { targetUserName: ?0 } }",
            "{ $facet: { " +
                    "  unread: [{ $match: { isRead: false } }, { $count: 'count' }], " +
                    "  total: [{ $count: 'count' }] " +
                    "} }",
            "{ $project: { " +
                    "  unread: { $ifNull: [{ $arrayElemAt: ['$unread.count', 0] }, 0] }, " +
                    "  total: { $ifNull: [{ $arrayElemAt: ['$total.count', 0] }, 0] } " +
                    "} }"
    })
    Mono<NotificationStatistics> gatherStatistics(String targetUserName);
}
