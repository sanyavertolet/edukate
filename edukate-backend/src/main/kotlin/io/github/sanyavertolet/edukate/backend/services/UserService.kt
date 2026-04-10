package io.github.sanyavertolet.edukate.backend.services

import io.github.sanyavertolet.edukate.backend.entities.User
import io.github.sanyavertolet.edukate.backend.repositories.UserRepository
import io.github.sanyavertolet.edukate.common.notifications.SimpleNotificationCreateRequest
import io.github.sanyavertolet.edukate.common.services.Notifier
import io.github.sanyavertolet.edukate.common.users.UserStatus
import io.github.sanyavertolet.edukate.common.utils.monoId
import java.util.UUID
import org.slf4j.LoggerFactory
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Service
class UserService(private val userRepository: UserRepository, private val notifier: Notifier) {
    fun findUserByAuthentication(authentication: Authentication): Mono<User> =
        authentication.monoId().flatMap { findUserById(it) }

    fun saveUser(user: User): Mono<User> = userRepository.save(user)

    fun findUserById(userId: String): Mono<User> = userRepository.findById(userId)

    fun findUserName(userId: String): Mono<String> =
        userRepository.findById(userId).map { it.name }.defaultIfEmpty(DEFAULT_USER_NAME)

    fun findUsersByIds(userIds: Collection<String>): Flux<User> = userRepository.findAllById(userIds)

    fun findUserByName(name: String): Mono<User> = userRepository.findByName(name)

    fun deleteUserById(id: String): Mono<Void> = userRepository.deleteById(id)

    fun hasUserPermissionToSubmit(user: User): Mono<Boolean> = Mono.just(user.status == UserStatus.ACTIVE)

    fun notifyAllUsersWithStatus(title: String?, message: String, status: UserStatus): Mono<Long> =
        userRepository
            .findAllByStatus(status)
            .map { user ->
                SimpleNotificationCreateRequest(
                    UUID.randomUUID().toString(),
                    requireNotNull(user.id),
                    title ?: "edukate-talks",
                    message,
                    "edukate team",
                )
            }
            .flatMap { req ->
                notifier.notify(req).onErrorResume { ex ->
                    log.warn("Could not notify user {}", req.targetUserId, ex)
                    Mono.empty()
                }
            }
            .count()

    companion object {
        private const val DEFAULT_USER_NAME = "UNKNOWN"
        private val log = LoggerFactory.getLogger(UserService::class.java)
    }
}
