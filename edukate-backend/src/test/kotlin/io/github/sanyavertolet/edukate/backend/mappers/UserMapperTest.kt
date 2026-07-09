package io.github.sanyavertolet.edukate.backend.mappers

import io.github.sanyavertolet.edukate.backend.BackendFixtures
import io.github.sanyavertolet.edukate.storage.configs.S3Properties
import java.time.Duration
import java.time.Instant
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class UserMapperTest {
    private val baseProps =
        S3Properties(
            endpoint = "http://minio:9000",
            region = "us-east-1",
            accessKey = "k",
            secretKey = "s",
            bucket = "edukate",
            signatureDuration = Duration.ofHours(1),
            publicEndpoint = null,
        )

    @Test
    fun `avatarUrl returns null when avatarUpdatedAt is null`() {
        val mapper = UserMapper(baseProps)
        val user = BackendFixtures.user(id = 42L, avatarUpdatedAt = null)
        assertThat(mapper.avatarUrl(user)).isNull()
        assertThat(mapper.toDto(user).avatarUrl).isNull()
    }

    @Test
    fun `avatarUrl returns null when user id is null`() {
        val mapper = UserMapper(baseProps)
        val user = BackendFixtures.user(id = null, avatarUpdatedAt = Instant.ofEpochSecond(1_700_000_000))
        assertThat(mapper.avatarUrl(user)).isNull()
    }

    @Test
    fun `avatarUrl encodes path and version from avatarUpdatedAt`() {
        val mapper = UserMapper(baseProps)
        val user = BackendFixtures.user(id = 42L, avatarUpdatedAt = Instant.ofEpochSecond(1_700_000_000))
        assertThat(mapper.avatarUrl(user)).isEqualTo("http://minio:9000/edukate/users/42/avatar/avatar.jpg?v=1700000000")
    }

    @Test
    fun `avatarUrl uses publicEndpoint when configured`() {
        val publicProps = baseProps.copy(publicEndpoint = "https://cdn.example.com")
        val mapper = UserMapper(publicProps)
        val user = BackendFixtures.user(id = 7L, avatarUpdatedAt = Instant.ofEpochSecond(123_456))
        assertThat(mapper.avatarUrl(user)).isEqualTo("https://cdn.example.com/edukate/users/7/avatar/avatar.jpg?v=123456")
    }

    @Test
    fun `avatarUrl version changes when avatarUpdatedAt advances`() {
        val mapper = UserMapper(baseProps)
        val first = BackendFixtures.user(id = 42L, avatarUpdatedAt = Instant.ofEpochSecond(1_700_000_000))
        val second = BackendFixtures.user(id = 42L, avatarUpdatedAt = Instant.ofEpochSecond(1_700_004_000))
        assertThat(mapper.avatarUrl(first)).isNotEqualTo(mapper.avatarUrl(second))
        assertThat(mapper.avatarUrl(first)).endsWith("?v=1700000000")
        assertThat(mapper.avatarUrl(second)).endsWith("?v=1700004000")
    }

    @Test
    fun `toDto and toInfoDto agree on avatar url`() {
        val mapper = UserMapper(baseProps)
        val user = BackendFixtures.user(id = 42L, avatarUpdatedAt = Instant.ofEpochSecond(1_700_000_000))
        val dto = mapper.toDto(user)
        val info = mapper.toInfoDto(user)
        assertThat(dto.avatarUrl).isEqualTo(info.avatarUrl)
        assertThat(info.name).isEqualTo(user.name)
    }
}
