package io.github.sanyavertolet.edukate.gateway.configs

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.factory.PasswordEncoderFactories
import org.springframework.security.crypto.password.DelegatingPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder

@Configuration
class PasswordEncoderConfig {
    @Bean
    fun passwordEncoder(): PasswordEncoder {
        val delegatingPasswordEncoder =
            PasswordEncoderFactories.createDelegatingPasswordEncoder() as DelegatingPasswordEncoder
        delegatingPasswordEncoder.setDefaultPasswordEncoderForMatches(BCryptPasswordEncoder())
        return delegatingPasswordEncoder
    }
}
