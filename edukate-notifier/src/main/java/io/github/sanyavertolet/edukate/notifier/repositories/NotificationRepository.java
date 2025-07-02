package io.github.sanyavertolet.edukate.notifier.repositories;

import io.github.sanyavertolet.edukate.notifier.entities.BaseNotification;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Repository
public interface NotificationRepository extends ReactiveMongoRepository<BaseNotification, String> {
    Mono<Long> countAllByUserIdAndIsRead(String userId, Boolean isRead);

    Mono<BaseNotification> findNotificationByUuid(String uuid);

    Flux<BaseNotification> findAllByUserIdAndIsRead(String userId, Boolean isRead, Pageable pageable);

    Flux<BaseNotification> findAllByUserId(String userId, Pageable pageable);

    Flux<BaseNotification> findByUuidInAndUserId(List<String> uuid, String userId);
}
