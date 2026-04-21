package io.github.sanyavertolet.edukate.backend.controllers.internal

import io.github.sanyavertolet.edukate.backend.entities.User
import io.github.sanyavertolet.edukate.backend.services.UserService
import io.github.sanyavertolet.edukate.common.users.UserCredentials
import io.github.sanyavertolet.edukate.common.users.UserStatus
import io.swagger.v3.oas.annotations.Hidden
import io.swagger.v3.oas.annotations.security.SecurityRequirements
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@Hidden
@RestController
@SecurityRequirements
@RequestMapping("/internal/users")
class UserInternalController(private val userService: UserService) {
    @PostMapping
    fun saveUser(@RequestBody userCredentials: UserCredentials): Mono<UserCredentials> =
        userService.saveUser(User.newFromCredentials(userCredentials)).map { it.toCredentials() }

    @GetMapping("/by-name/{name}")
    fun getUserByName(@PathVariable name: String): Mono<UserCredentials> =
        userService.findUserByName(name).map { it.toCredentials() }

    @GetMapping("/by-id/{id}")
    fun getUserById(@PathVariable id: Long): Mono<UserCredentials> = userService.findUserById(id).map { it.toCredentials() }

    @DeleteMapping("/by-id/{id}") fun deleteUserById(@PathVariable id: Long): Mono<Void> = userService.deleteUserById(id)

    @PostMapping("/notify-all")
    fun notifyAllUsers(
        @RequestBody body: Map<String, String>,
        @RequestParam(required = false, defaultValue = "ACTIVE") status: UserStatus,
    ): Mono<Long> = userService.notifyAllUsersWithStatus(body["title"], requireNotNull(body["message"]), status)
}
