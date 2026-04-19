package io.github.sanyavertolet.edukate.gateway.services

import io.github.sanyavertolet.edukate.common.users.EdukateUserDetails
import io.github.sanyavertolet.edukate.common.users.UserCredentials
import org.springframework.security.core.userdetails.ReactiveUserDetailsPasswordService
import org.springframework.security.core.userdetails.ReactiveUserDetailsService
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class UserDetailsService(private val backendService: BackendService) :
    ReactiveUserDetailsService, ReactiveUserDetailsPasswordService {

    override fun findByUsername(username: String): Mono<UserDetails> =
        backendService.getUserByName(username).map(::EdukateUserDetails)

    fun findEdukateUserDetailsByUsername(username: String): Mono<EdukateUserDetails> =
        backendService.getUserByName(username).map(::EdukateUserDetails)

    fun findById(id: Long): Mono<EdukateUserDetails> = backendService.getUserById(id).map(::EdukateUserDetails)

    fun isNotUserPresent(username: String): Mono<Boolean> =
        backendService.getUserByName(username).map { false }.defaultIfEmpty(true)

    // todo: looks like this dummy stub needs to be fixed
    override fun updatePassword(user: UserDetails, newPassword: String?): Mono<UserDetails> = Mono.just(user)

    fun create(username: String, email: String, encodedPassword: String): Mono<EdukateUserDetails> {
        val userCredentials = UserCredentials.newUser(username, encodedPassword, email)
        return backendService.saveUser(userCredentials).map(::EdukateUserDetails).doOnNext { it.eraseCredentials() }
    }
}
