package io.github.sanyavertolet.edukate.notifier.services;

import io.github.sanyavertolet.edukate.notifier.dtos.NotificationStatistics;
import io.github.sanyavertolet.edukate.notifier.entities.BaseNotification;
import io.github.sanyavertolet.edukate.notifier.repositories.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;

    public Mono<BaseNotification> saveIfAbsent(BaseNotification notification) {
        return notificationRepository.findNotificationByUuid(notification.getUuid())
                .doOnNext(existingNotification -> log.debug(
                        "Found existing notification with UUID {}: {}",
                        notification.getUuid(), existingNotification
                ))
                .switchIfEmpty(notificationRepository.save(notification).doOnSuccess(savedNotification ->
                        log.info("Saved new notification with UUID {}: {}", notification.getUuid(), savedNotification)
                ));
    }

    public Flux<BaseNotification> getUserNotifications(
            Boolean isRead, int size, int page, Authentication authentication
    ) {
        return Mono.just(PageRequest.of(page, size, Sort.Direction.DESC, "createdAt"))
                .flatMapMany(pageRequest -> {
                    if (isRead == null) {
                        return notificationRepository.findAllByUserId(authentication.getName(), pageRequest);
                    }
                    return notificationRepository.findAllByUserIdAndIsRead(authentication.getName(), isRead, pageRequest);
                });
    }

    public Mono<NotificationStatistics> gatherUserStatistics(Authentication authentication) {
        return notificationRepository.gatherStatistics(authentication.getName());
    }

    @Transactional
    public Mono<Long> markAsRead(List<String> uuids, Authentication authentication) {
        return notificationRepository.findByUuidInAndUserId(uuids, authentication.getName())
                .map(notification -> {
                    notification.setIsRead(Boolean.TRUE);
                    return notification;
                })
                .flatMap(notificationRepository::save)
                .count();
    }

    @Transactional
    public Mono<Long> markAllAsRead(Authentication authentication) {
        return notificationRepository.findAllByUserIdAndIsRead(authentication.getName(), Boolean.FALSE, Pageable.unpaged())
                .map(notification -> {
                    notification.setIsRead(Boolean.TRUE);
                    return notification;
                })
                .flatMap(notificationRepository::save)
                .count();
    }
}
