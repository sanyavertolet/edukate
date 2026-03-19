package io.github.sanyavertolet.edukate.common.utils

import io.github.sanyavertolet.edukate.common.users.EdukateUserDetails
import org.springframework.security.core.Authentication
import reactor.core.publisher.Mono

object AuthUtils {
    @JvmStatic
    fun id(authentication: Authentication?): String? = authentication?.let { it.principal as EdukateUserDetails }?.id

    @JvmStatic
    fun monoId(authentication: Authentication?): Mono<String> = Mono.justOrEmpty(id(authentication))
}
