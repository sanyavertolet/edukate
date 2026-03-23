package io.github.sanyavertolet.edukate.gateway.services

import io.github.sanyavertolet.edukate.common.users.EdukateUserDetails
import io.github.sanyavertolet.edukate.common.users.UserCredentials
import org.springframework.security.core.userdetails.ReactiveUserDetailsService
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class UserDetailsService(private val backendService: BackendService) : ReactiveUserDetailsService {
    override fun findByUsername(username: String): Mono<UserDetails> = findEdukateUserDetailsByUsername(username).map { it }

    fun findEdukateUserDetailsByUsername(username: String): Mono<EdukateUserDetails> =
        backendService.getUserByName(username).map(::EdukateUserDetails)

    fun findById(id: String): Mono<EdukateUserDetails> = backendService.getUserById(id).map(::EdukateUserDetails)

    fun isNotUserPresent(username: String): Mono<Boolean> = findByUsername(username).hasElement().map { !it }

    fun create(username: String, email: String, encodedPassword: String): Mono<EdukateUserDetails> {
        val userCredentials = UserCredentials.newUser(username, encodedPassword, email)
        return backendService.saveUser(userCredentials).map(::EdukateUserDetails).doOnNext { it.eraseCredentials() }
    }
}
