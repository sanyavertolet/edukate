package io.github.sanyavertolet.edukate.backend.services

import io.github.sanyavertolet.edukate.backend.entities.User
import io.github.sanyavertolet.edukate.backend.repositories.UserRepository
import io.github.sanyavertolet.edukate.common.notifications.SimpleNotificationCreateRequest
import io.github.sanyavertolet.edukate.common.services.Notifier
import io.github.sanyavertolet.edukate.common.users.UserStatus
import java.util.UUID
import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.cache.annotation.Caching
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

@Service
class UserService(private val userRepository: UserRepository, private val notifier: Notifier) {
    @Caching(
        evict =
            [
                CacheEvict(cacheNames = ["users-by-id"], key = "#user.id"),
                CacheEvict(cacheNames = ["users-by-name"], key = "#user.name"),
            ]
    )
    fun saveUser(user: User): Mono<User> = userRepository.save(user)

    @Cacheable(cacheNames = ["users-by-id"], key = "#userId")
    fun findUserById(userId: String): Mono<User> = userRepository.findById(userId)

    @Cacheable(cacheNames = ["users-by-name"], key = "#name")
    fun findUserByName(name: String): Mono<User> = userRepository.findByName(name)

    @Caching(
        evict =
            [
                CacheEvict(cacheNames = ["users-by-id"], key = "#id"),
                CacheEvict(cacheNames = ["users-by-name"], allEntries = true),
            ]
    )
    fun deleteUserById(id: String): Mono<Void> = userRepository.deleteById(id)

    fun hasUserPermissionToSubmit(user: User): Mono<Boolean> = (user.status == UserStatus.ACTIVE).toMono()

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
        private val log = LoggerFactory.getLogger(UserService::class.java)
    }
}
