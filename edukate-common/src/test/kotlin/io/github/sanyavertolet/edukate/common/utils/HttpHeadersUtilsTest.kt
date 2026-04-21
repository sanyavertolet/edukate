package io.github.sanyavertolet.edukate.common.utils

import io.github.sanyavertolet.edukate.common.CommonFixtures
import io.github.sanyavertolet.edukate.common.users.UserRole
import io.github.sanyavertolet.edukate.common.users.UserStatus
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.http.HttpHeaders

class HttpHeadersUtilsTest {

    @Test
    fun `populateHeaders sets all four X-Authorization headers`() {
        val headers = HttpHeaders()
        val details = CommonFixtures.userDetails(id = 1L, name = "alice", status = UserStatus.ACTIVE)

        populateHeaders(headers, details)

        assertThat(headers.getFirst(AuthHeaders.AUTHORIZATION_ID.headerName)).isEqualTo("1")
        assertThat(headers.getFirst(AuthHeaders.AUTHORIZATION_NAME.headerName)).isEqualTo("alice")
        assertThat(headers.getFirst(AuthHeaders.AUTHORIZATION_STATUS.headerName)).isEqualTo("ACTIVE")
        assertThat(headers.getFirst(AuthHeaders.AUTHORIZATION_ROLES.headerName)).isEqualTo("USER")
    }

    @Test
    fun `toEdukateUserDetails round-trip preserves all fields`() {
        val details =
            CommonFixtures.userDetails(
                id = 2L,
                name = "bob",
                roles = setOf(UserRole.ADMIN, UserRole.MODERATOR),
                status = UserStatus.PENDING,
            )
        val headers = HttpHeaders()
        populateHeaders(headers, details)

        val parsed = headers.toEdukateUserDetails()

        assertThat(parsed).isNotNull
        assertThat(parsed!!.id).isEqualTo(2L)
        assertThat(parsed.username).isEqualTo("bob")
        assertThat(parsed.roles).containsExactlyInAnyOrder(UserRole.ADMIN, UserRole.MODERATOR)
        assertThat(parsed.status).isEqualTo(UserStatus.PENDING)
    }

    @Test
    fun `toEdukateUserDetails returns null when any required header is missing`() {
        fun headersWithout(omit: AuthHeaders): HttpHeaders {
            val h = HttpHeaders()
            populateHeaders(h, CommonFixtures.userDetails())
            h.remove(omit.headerName)
            return h
        }

        assertThat(headersWithout(AuthHeaders.AUTHORIZATION_ID).toEdukateUserDetails()).isNull()
        assertThat(headersWithout(AuthHeaders.AUTHORIZATION_NAME).toEdukateUserDetails()).isNull()
        assertThat(headersWithout(AuthHeaders.AUTHORIZATION_ROLES).toEdukateUserDetails()).isNull()
        assertThat(headersWithout(AuthHeaders.AUTHORIZATION_STATUS).toEdukateUserDetails()).isNull()
    }

    @Test
    fun `toEdukateUserDetails uses last value when header has multiple values`() {
        val headers = HttpHeaders()
        populateHeaders(headers, CommonFixtures.userDetails(id = 1L))
        headers.add(AuthHeaders.AUTHORIZATION_ID.headerName, "99")

        val parsed = headers.toEdukateUserDetails()

        assertThat(parsed!!.id).isEqualTo(99L)
    }
}
