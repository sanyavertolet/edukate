package io.github.sanyavertolet.edukate.backend.configs

import org.flywaydb.core.Flyway
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class FlywayConfig(
    @param:Value("\${spring.flyway.url}") private val url: String,
    @param:Value("\${spring.flyway.user}") private val user: String,
    @param:Value("\${spring.flyway.password}") private val password: String,
) {
    @Bean(initMethod = "migrate") fun flyway(): Flyway = Flyway.configure().dataSource(url, user, password).load()
}
