package io.github.sanyavertolet.edukate.notifier.controllers;

import io.github.sanyavertolet.edukate.notifier.dtos.BaseNotificationDto;
import io.github.sanyavertolet.edukate.notifier.dtos.NotificationStatistics;
import io.github.sanyavertolet.edukate.notifier.entities.BaseNotification;
import io.github.sanyavertolet.edukate.notifier.services.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.validation.annotation.Validated;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

import static io.swagger.v3.oas.annotations.enums.ParameterIn.QUERY;

@Slf4j
@RequiredArgsConstructor
@RestController
@Validated
@RequestMapping("/api/v1/notifications")
@Tag(name = "Notifications", description = "API for managing user notifications")
public class NotificationController {
    private final NotificationService notificationService;

    @PostMapping("/mark-as-read")
    @Operation(
            summary = "Mark notifications as read",
            description = "Marks the specified notifications as read for the authenticated user"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully marked notifications as read",
                    content = @Content(schema = @Schema(implementation = Long.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
    })
    public Mono<Long> markAsRead(@RequestBody @Valid @NotEmpty List<@NotBlank String> uuids, Authentication authentication) {
        return notificationService.markAsRead(uuids, authentication);
    }

    @PostMapping("/mark-all-as-read")
    @Operation(
            summary = "Mark all notifications as read",
            description = "Marks all notifications as read for the authenticated user"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully marked all notifications as read",
                    content = @Content(schema = @Schema(implementation = Long.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
    })
    public Mono<Long> markAsRead(Authentication authentication) {
        return notificationService.markAllAsRead(authentication);
    }

    @GetMapping
    @Operation(
            summary = "Get user notifications",
            description = "Retrieves paginated notifications for the authenticated user"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved notifications", 
                    content = @Content(schema = @Schema(implementation = BaseNotificationDto.class))),
        @ApiResponse(responseCode = "400", description = "Validation failed", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
    })
    @Parameters({
            @Parameter(name = "isRead", description = "Filter by read status (null for all notifications)", in = QUERY),
            @Parameter(name = "page", description = "Page number (zero-based)", in = QUERY,
                    schema = @Schema(minimum = "0")),
            @Parameter(name = "size", description = "Number of notifications per page", in = QUERY,
                    schema = @Schema(minimum = "1", maximum = "100")),
    })
    public Flux<BaseNotificationDto> getNotifications(
            @RequestParam(defaultValue = "10") @PositiveOrZero int size,
            @RequestParam(defaultValue = "0") @Positive int page,
            @RequestParam(required = false) Boolean isRead,
            Authentication authentication
    ) {
        return Mono.justOrEmpty(authentication)
                .flatMapMany(auth -> notificationService.getUserNotifications(isRead, size, page, auth))
                .map(BaseNotification::toDto);
    }

    @GetMapping("/count")
    @Operation(
            summary = "Get notification statistics",
            description = "Retrieves notification statistics for the authenticated user"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved notification statistics",
                    content = @Content(schema = @Schema(implementation = NotificationStatistics.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
    })
    public Mono<NotificationStatistics> getNotificationsCount(Authentication authentication) {
        return notificationService.gatherUserStatistics(authentication);
    }
}
