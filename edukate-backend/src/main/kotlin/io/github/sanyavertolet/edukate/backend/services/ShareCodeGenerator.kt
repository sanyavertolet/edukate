package io.github.sanyavertolet.edukate.backend.services

import java.security.SecureRandom
import org.springframework.stereotype.Component

@Component
class ShareCodeGenerator {
    private val random = SecureRandom()

    fun generateShareCode(): String =
        (1..CODE_LENGTH).map { ALLOWED_CHARACTERS[random.nextInt(ALLOWED_CHARACTERS.length)] }.joinToString("")

    companion object {
        private const val ALLOWED_CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
        private const val CODE_LENGTH = 10
    }
}
