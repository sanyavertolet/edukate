package io.github.sanyavertolet.edukate.common.utils

import io.github.sanyavertolet.edukate.common.users.EdukateUserDetails
import org.springframework.security.core.Authentication
import reactor.kotlin.core.publisher.toMono

fun Authentication?.id(): Long? = this?.let { it.principal as EdukateUserDetails }?.id

fun Authentication?.monoId() = id().toMono()
