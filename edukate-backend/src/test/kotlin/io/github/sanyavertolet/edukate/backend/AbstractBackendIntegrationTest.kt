package io.github.sanyavertolet.edukate.backend

import com.ninjasquad.springmockk.MockkBean
import io.github.sanyavertolet.edukate.backend.storage.FileKeyStorage
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers
import org.springframework.test.web.reactive.server.WebTestClient

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureWebTestClient
abstract class AbstractBackendIntegrationTest {

    @Autowired protected lateinit var webTestClient: WebTestClient

    @MockkBean(relaxed = true) protected lateinit var rabbitTemplate: RabbitTemplate

    @MockkBean protected lateinit var fileKeyStorage: FileKeyStorage

    protected fun authenticatedClient(userId: String = "user-1", username: String = "testuser"): WebTestClient =
        webTestClient.mutateWith(
            SecurityMockServerConfigurers.mockAuthentication(
                BackendFixtures.mockAuthentication(userId = userId, username = username)
            )
        )
}
