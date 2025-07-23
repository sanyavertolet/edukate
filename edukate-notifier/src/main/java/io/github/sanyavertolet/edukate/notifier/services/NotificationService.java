package io.github.sanyavertolet.edukate.notifier.services;

import io.github.sanyavertolet.edukate.common.utils.AuthUtils;
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

    @Transactional
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
        return AuthUtils.monoId(authentication)
                .flatMapMany(userId -> {
                    PageRequest pageRequest = PageRequest.of(page, size, Sort.Direction.DESC, "createdAt");
                    if (isRead == null) {
                        return notificationRepository.findAllByTargetUserId(userId, pageRequest);
                    }
                    return notificationRepository.findAllByTargetUserIdAndIsRead(userId, isRead, pageRequest);
                });
    }

    public Mono<NotificationStatistics> gatherUserStatistics(Authentication authentication) {
        return AuthUtils.monoId(authentication).flatMap(notificationRepository::gatherStatistics);
    }

    @Transactional
    public Mono<Long> markAsRead(List<String> uuids, Authentication authentication) {
        return AuthUtils.monoId(authentication)
                .flatMapMany(userId -> notificationRepository.findByTargetUserIdAndUuidIn(userId, uuids))
                .map(notification -> {
                    notification.setIsRead(Boolean.TRUE);
                    return notification;
                })
                .flatMap(notificationRepository::save)
                .count();
    }

    @Transactional
    public Mono<Long> markAllAsRead(Authentication authentication) {
        return AuthUtils.monoId(authentication)
                .flatMapMany(userId ->
                        notificationRepository.findAllByTargetUserIdAndIsRead(userId, Boolean.FALSE, Pageable.unpaged())
                )
                .map(notification -> {
                    notification.setIsRead(Boolean.TRUE);
                    return notification;
                })
                .flatMap(notificationRepository::save)
                .count();
    }
}
