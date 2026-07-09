package io.github.sanyavertolet.edukate.backend.services

import io.github.sanyavertolet.edukate.backend.entities.User
import io.github.sanyavertolet.edukate.backend.repositories.UserRepository
import io.github.sanyavertolet.edukate.common.notifications.SimpleNotificationCreateRequest
import io.github.sanyavertolet.edukate.common.services.Notifier
import io.github.sanyavertolet.edukate.common.users.UserStatus
import io.github.sanyavertolet.edukate.common.utils.orNotFound
import java.time.Instant
import java.util.UUID
import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.cache.annotation.Caching
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

@Service
class UserService(private val userRepository: UserRepository, private val notifier: Notifier) {
    @Caching(
        evict =
            [
                CacheEvict(cacheNames = ["users-by-id"], key = "#user.id", condition = "#user.id != null"),
                CacheEvict(cacheNames = ["users-by-name"], key = "#user.name"),
            ]
    )
    fun saveUser(user: User): Mono<User> = userRepository.save(user)

    @Cacheable(cacheNames = ["users-by-id"], key = "#userId")
    fun findUserById(userId: Long): Mono<User> = userRepository.findById(userId)

    @Cacheable(cacheNames = ["users-by-name"], key = "#name")
    fun findUserByName(name: String): Mono<User> = userRepository.findByName(name)

    fun findUserByEmail(email: String): Mono<User> = userRepository.findByEmail(email)

    @Caching(
        evict =
            [
                CacheEvict(cacheNames = ["users-by-id"], key = "#id"),
                CacheEvict(cacheNames = ["users-by-name"], allEntries = true),
            ]
    )
    fun deleteUserById(id: Long): Mono<Void> = userRepository.deleteById(id)

    fun getUserNamesByPrefix(prefix: String, limit: Int, excludeIds: Set<Long> = emptySet()): Flux<String> =
        userRepository
            .findByNamePrefix(prefix, limit + excludeIds.size)
            .filter { it.id !in excludeIds }
            .take(limit.toLong())
            .map { it.name }

    fun hasUserPermissionToSubmit(user: User): Mono<Boolean> = (user.status == UserStatus.ACTIVE).toMono()

    @Caching(
        evict =
            [
                CacheEvict(cacheNames = ["users-by-id"], key = "#userId"),
                CacheEvict(cacheNames = ["users-by-name"], allEntries = true),
            ]
    )
    fun updateName(userId: Long, newName: String): Mono<User> =
        userRepository
            .findByName(newName)
            .flatMap<User> {
                Mono.error(ResponseStatusException(HttpStatus.CONFLICT, "Username '$newName' is already taken"))
            }
            .switchIfEmpty(
                userRepository.findById(userId).orNotFound("User not found").flatMap {
                    userRepository.save(it.copy(name = newName))
                }
            )

    @Caching(
        evict =
            [
                CacheEvict(cacheNames = ["users-by-id"], key = "#userId"),
                CacheEvict(cacheNames = ["users-by-name"], allEntries = true),
            ]
    )
    fun updateEncodedPassword(userId: Long, encodedPassword: String): Mono<Void> =
        userRepository
            .findById(userId)
            .orNotFound("User not found")
            .flatMap { userRepository.save(it.copy(token = encodedPassword)) }
            .then()

    @Caching(
        evict =
            [
                CacheEvict(cacheNames = ["users-by-id"], key = "#userId"),
                CacheEvict(cacheNames = ["users-by-name"], allEntries = true),
            ]
    )
    fun setAvatarTimestamp(userId: Long, instant: Instant?): Mono<User> =
        userRepository.findById(userId).orNotFound("User not found").flatMap {
            userRepository.save(it.copy(avatarUpdatedAt = instant))
        }

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
