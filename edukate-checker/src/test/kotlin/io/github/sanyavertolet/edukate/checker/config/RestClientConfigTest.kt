@file:Suppress("MagicNumber")

package io.github.sanyavertolet.edukate.checker.config

import io.github.sanyavertolet.edukate.checker.configs.RestClientConfig
import java.net.ServerSocket
import java.time.Duration
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.web.client.ResourceAccessException

class RestClientConfigTest {
    private val config = RestClientConfig()

    @Test
    fun `restClientBuilder creates a non-null builder`() {
        val builder = config.restClientBuilder(Duration.ofSeconds(5), Duration.ofMinutes(2))
        assertThat(builder).isNotNull()
    }

    @Test
    fun `read timeout is enforced`() {
        val readTimeout = Duration.ofMillis(200)
        val builder = config.restClientBuilder(Duration.ofSeconds(5), readTimeout)

        ServerSocket(0).use { serverSocket ->
            // Accept the TCP connection but never send a response, triggering the read timeout.
            Thread { serverSocket.accept() }.also { it.isDaemon = true }.start()

            val client = builder.baseUrl("http://localhost:${serverSocket.localPort}").build()

            val start = System.nanoTime()
            assertThatThrownBy { client.get().uri("/").retrieve().toBodilessEntity() }
                .isInstanceOf(ResourceAccessException::class.java)
            val elapsed = Duration.ofNanos(System.nanoTime() - start)

            assertThat(elapsed).isGreaterThanOrEqualTo(readTimeout)
        }
    }
}
