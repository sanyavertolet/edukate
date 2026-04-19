package io.github.sanyavertolet.edukate.backend

import com.ninjasquad.springmockk.MockkBean
import io.github.sanyavertolet.edukate.backend.storage.FileKeyStorage
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.web.reactive.server.WebTestClient

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureWebTestClient
abstract class AbstractBackendIntegrationTest {

    @Autowired protected lateinit var webTestClient: WebTestClient

    @MockkBean(relaxed = true) protected lateinit var rabbitTemplate: RabbitTemplate

    @MockkBean protected lateinit var fileKeyStorage: FileKeyStorage

    protected fun authenticatedClient(userId: Long = 1L, username: String = "testuser"): WebTestClient =
        webTestClient.mutateWith(
            SecurityMockServerConfigurers.mockAuthentication(
                BackendFixtures.mockAuthentication(userId = userId, username = username)
            )
        )

    companion object {
        @JvmStatic
        @DynamicPropertySource
        fun configureProperties(registry: DynamicPropertyRegistry) {
            val pg = PostgresTestContainer.instance
            registry.add("spring.r2dbc.url") { "r2dbc:postgresql://${pg.host}:${pg.getMappedPort(5432)}/${pg.databaseName}" }
            registry.add("spring.r2dbc.username") { pg.username }
            registry.add("spring.r2dbc.password") { pg.password }
            registry.add("spring.flyway.url") { pg.jdbcUrl }
            registry.add("spring.flyway.user") { pg.username }
            registry.add("spring.flyway.password") { pg.password }
            registry.add("spring.datasource.url") { pg.jdbcUrl }
            registry.add("spring.datasource.username") { pg.username }
            registry.add("spring.datasource.password") { pg.password }
        }
    }
}
