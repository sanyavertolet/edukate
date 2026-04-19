package io.github.sanyavertolet.edukate.backend

import org.flywaydb.core.Flyway
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName

object PostgresTestContainer {
    val instance: PostgreSQLContainer<*> =
        PostgreSQLContainer(DockerImageName.parse("postgres:18")).apply {
            start()
            Flyway.configure().dataSource(jdbcUrl, username, password).locations("classpath:db/migration").load().migrate()
        }
}
