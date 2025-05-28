package io.github.sanyavertolet.edukate.notifier.controllers;

import io.github.sanyavertolet.edukate.notifier.dtos.BaseNotificationDto;
import io.github.sanyavertolet.edukate.notifier.entities.BaseNotification;
import io.github.sanyavertolet.edukate.notifier.services.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {
    private final NotificationService notificationService;

    @PostMapping("/mark-as-read")
    public Mono<Long> markAsRead(@RequestBody List<String> uuids, Authentication authentication) {
        return notificationService.markAsRead(uuids, authentication);
    }

    @GetMapping
    @PreAuthorize("hasRole('ROLE_USER')")
    public Flux<BaseNotificationDto> getNotifications(
            @RequestParam(required = false, defaultValue = "10") Integer size,
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "false") Boolean isRead,
            Authentication authentication
    ) {
        return Mono.justOrEmpty(authentication).flatMapMany(auth ->
                notificationService.getUserNotifications(isRead, Pageable.ofSize(size).withPage(page), auth)
        )
                .map(BaseNotification::toDto);
    }

    @GetMapping("/count")
    @PreAuthorize("hasRole('ROLE_USER')")
    public Mono<Long> getNotificationsCount(
            @RequestParam(required = false, defaultValue = "false") Boolean isRead,
            Authentication authentication
    ) {
        return notificationService.countAllUserNotifications(isRead, authentication);
    }
}
