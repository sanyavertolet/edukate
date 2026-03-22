package io.github.sanyavertolet.edukate.notifier.controllers

import io.github.sanyavertolet.edukate.notifier.dtos.BaseNotificationDto
import io.github.sanyavertolet.edukate.notifier.dtos.NotificationStatistics
import io.github.sanyavertolet.edukate.notifier.services.NotificationService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.Parameters
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import jakarta.validation.constraints.NotEmpty
import lombok.RequiredArgsConstructor
import lombok.extern.slf4j.Slf4j
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/notifications")
@Tag(name = "Notifications", description = "API for managing user notifications")
class NotificationController(private val notificationService: NotificationService) {
    @PostMapping("/mark-as-read")
    @Operation(
        summary = "Mark notifications as read",
        description = "Marks the specified notifications as read for the authenticated user",
    )
    @ApiResponses(
        value =
            [
                ApiResponse(
                    responseCode = "200",
                    description = "Successfully marked notifications as read",
                    content = arrayOf(Content(schema = Schema(implementation = Long::class))),
                ),
                ApiResponse(responseCode = "400", description = "Validation failed"),
                ApiResponse(responseCode = "401", description = "Unauthorized"),
            ]
    )
    fun markAsRead(
        @RequestBody @Valid @NotEmpty uuids: @Valid @NotEmpty List<String>,
        authentication: Authentication?,
    ): Mono<Long> = notificationService.markAsRead(uuids, authentication)

    @PostMapping("/mark-all-as-read")
    @Operation(
        summary = "Mark all notifications as read",
        description = "Marks all notifications as read for the authenticated user",
    )
    @ApiResponses(
        value =
            [
                ApiResponse(
                    responseCode = "200",
                    description = "Successfully marked all notifications as read",
                    content = arrayOf(Content(schema = Schema(implementation = Long::class))),
                ),
                ApiResponse(responseCode = "401", description = "Unauthorized"),
            ]
    )
    fun markAsRead(authentication: Authentication?): Mono<Long> = notificationService.markAllAsRead(authentication)

    @GetMapping
    @Operation(
        summary = "Get user notifications",
        description = "Retrieves paginated notifications for the authenticated user",
    )
    @ApiResponses(
        value =
            [
                ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved notifications",
                    content = arrayOf(Content(schema = Schema(implementation = BaseNotificationDto::class))),
                ),
                ApiResponse(responseCode = "400", description = "Validation failed"),
                ApiResponse(responseCode = "401", description = "Unauthorized"),
            ]
    )
    @Parameters(
        Parameter(
            name = "isRead",
            description = "Filter by read status (null for all notifications)",
            `in` = ParameterIn.QUERY,
        ),
        Parameter(
            name = "page",
            description = "Page number (zero-based)",
            `in` = ParameterIn.QUERY,
            schema = Schema(minimum = "0"),
        ),
        Parameter(
            name = "size",
            description = "Number of notifications per page",
            `in` = ParameterIn.QUERY,
            schema = Schema(minimum = "1", maximum = "100"),
        ),
    )
    fun getNotifications(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int,
        @RequestParam(required = false) isRead: Boolean?,
        authentication: Authentication?,
    ): Flux<BaseNotificationDto> =
        notificationService.getUserNotifications(isRead, size, page, authentication).map { it.toDto() }

    @GetMapping("/count")
    @Operation(
        summary = "Get notification statistics",
        description = "Retrieves notification statistics for the authenticated user",
    )
    @ApiResponses(
        value =
            [
                ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved notification statistics",
                    content = arrayOf(Content(schema = Schema(implementation = NotificationStatistics::class))),
                ),
                ApiResponse(responseCode = "401", description = "Unauthorized"),
            ]
    )
    fun getNotificationsCount(authentication: Authentication?): Mono<NotificationStatistics> =
        notificationService.gatherUserStatistics(authentication)
}
