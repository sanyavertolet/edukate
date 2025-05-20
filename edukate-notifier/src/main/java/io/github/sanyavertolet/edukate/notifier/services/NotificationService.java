package io.github.sanyavertolet.edukate.notifier.services;

import io.github.sanyavertolet.edukate.notifier.entities.BaseNotification;
import io.github.sanyavertolet.edukate.notifier.repositories.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
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

    public Flux<BaseNotification> getAllUserNotifications(Authentication authentication, Boolean isRead, Pageable pageable) {
        return notificationRepository.findAllByUserIdAndIsRead(authentication.getName(), isRead, pageable);
    }

    public Mono<Long> countAllUserNotifications(Authentication authentication, Boolean isRead) {
        return notificationRepository.countAllByUserIdAndIsRead(authentication.getName(), isRead).defaultIfEmpty(0L);
    }

    @Transactional
    public Mono<Long> markAsRead(List<String> uuids, Authentication authentication) {
        return notificationRepository.findByUuidInAndUserId(uuids, authentication.getName())
                .doOnNext(notification -> notification.setIsRead(Boolean.TRUE))
                .flatMap(notificationRepository::save)
                .map(BaseNotification::getIsRead)
                .map(read -> read == Boolean.TRUE)
                .count();
    }
}
