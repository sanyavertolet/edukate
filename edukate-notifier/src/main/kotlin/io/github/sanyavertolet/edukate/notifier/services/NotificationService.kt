package io.github.sanyavertolet.edukate.notifier.services

import io.github.sanyavertolet.edukate.common.notifications.BaseNotificationCreateRequest
import io.github.sanyavertolet.edukate.common.utils.monoId
import io.github.sanyavertolet.edukate.notifier.dtos.NotificationStatistics
import io.github.sanyavertolet.edukate.notifier.entities.BaseNotification
import io.github.sanyavertolet.edukate.notifier.repositories.NotificationRepository
import org.slf4j.LoggerFactory
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Service
class NotificationService(
    private val notificationRepository: NotificationRepository,
    private val mongoTemplate: ReactiveMongoTemplate,
) {
    @Transactional
    fun saveIfAbsent(createRequest: BaseNotificationCreateRequest): Mono<BaseNotification> =
        Mono.just(createRequest).map { BaseNotification.fromCreationRequest(it) }.flatMap { saveIfAbsent(it) }

    // TODO: this find-then-save flow is race-prone under concurrent same-UUID writes; replace with
    // atomic upsert/duplicate-key recovery
    @Transactional
    fun saveIfAbsent(notification: BaseNotification): Mono<BaseNotification> =
        notificationRepository
            .findNotificationByUuid(notification.uuid)
            .doOnNext { log.debug("Found existing notification with UUID {}: {}", it.uuid, it) }
            .switchIfEmpty(
                Mono.defer {
                    notificationRepository.save(notification).doOnNext {
                        log.debug("Saved new notification with UUID {}: {}", it.uuid, it)
                    }
                }
            )
            .doOnError { log.error("Error saving notification: {}", notification.uuid, it) }

    fun getUserNotifications(
        isRead: Boolean?,
        size: Int,
        page: Int,
        authentication: Authentication?,
    ): Flux<BaseNotification> =
        authentication.monoId().flatMapMany { userId ->
            val pageRequest = PageRequest.of(page, size, Sort.Direction.DESC, "createdAt")
            isRead?.let { notificationRepository.findAllByTargetUserIdAndIsRead(userId, isRead, pageRequest) }
                ?: notificationRepository.findAllByTargetUserId(userId, pageRequest)
        }

    fun gatherUserStatistics(authentication: Authentication?): Mono<NotificationStatistics> =
        authentication
            .monoId()
            .flatMap { notificationRepository.gatherStatistics(it) }
            .defaultIfEmpty(NotificationStatistics())

    fun markAsRead(uuids: List<String>, authentication: Authentication?): Mono<Long> =
        authentication.monoId().flatMap { userId ->
            val query = Query(Criteria.where("targetUserId").`is`(userId).and("uuid").`in`(uuids).and("isRead").`is`(false))
            val update = Update.update("isRead", true)
            mongoTemplate.updateMulti(query, update, BaseNotification::class.java).map { it.modifiedCount }
        }

    fun markAllAsRead(authentication: Authentication?): Mono<Long> =
        authentication.monoId().flatMap { userId ->
            val query = Query(Criteria.where("targetUserId").`is`(userId).and("isRead").`is`(false))
            val update = Update.update("isRead", true)
            mongoTemplate.updateMulti(query, update, BaseNotification::class.java).map { it.modifiedCount }
        }

    companion object {
        private val log = LoggerFactory.getLogger(NotificationService::class.java)
    }
}
