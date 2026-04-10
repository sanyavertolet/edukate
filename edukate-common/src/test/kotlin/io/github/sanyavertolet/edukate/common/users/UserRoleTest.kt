package io.github.sanyavertolet.edukate.common.users

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.security.core.authority.SimpleGrantedAuthority

class UserRoleTest {

    @Test
    fun `asSpringSecurityRole returns ROLE_ prefixed name for each role`() {
        assertThat(UserRole.USER.asSpringSecurityRole()).isEqualTo("ROLE_USER")
        assertThat(UserRole.MODERATOR.asSpringSecurityRole()).isEqualTo("ROLE_MODERATOR")
        assertThat(UserRole.ADMIN.asSpringSecurityRole()).isEqualTo("ROLE_ADMIN")
    }

    @Test
    fun `asGrantedAuthority returns SimpleGrantedAuthority with correct authority`() {
        assertThat(UserRole.USER.asGrantedAuthority()).isInstanceOf(SimpleGrantedAuthority::class.java)
        assertThat(UserRole.USER.asGrantedAuthority().authority).isEqualTo("ROLE_USER")
        assertThat(UserRole.ADMIN.asGrantedAuthority().authority).isEqualTo("ROLE_ADMIN")
    }

    @Test
    fun `listToString produces comma-separated role names`() {
        assertThat(UserRole.listToString(setOf(UserRole.USER))).isEqualTo("USER")
        assertThat(UserRole.listToString(emptySet())).isEmpty()
        val multiResult = UserRole.listToString(setOf(UserRole.ADMIN, UserRole.MODERATOR))
        assertThat(multiResult).contains("ADMIN").contains("MODERATOR").contains(",")
    }

    @Test
    fun `fromString parses role names correctly`() {
        assertThat(UserRole.fromString("USER")).containsExactly(UserRole.USER)
        assertThat(UserRole.fromString("ADMIN,MODERATOR")).containsExactlyInAnyOrder(UserRole.ADMIN, UserRole.MODERATOR)
        assertThat(UserRole.fromString("USER , ADMIN")).containsExactlyInAnyOrder(UserRole.USER, UserRole.ADMIN)
        assertThat(UserRole.fromString(null)).isEmpty()
    }

    @Test
    fun `fromString empty string throws IllegalArgumentException`() {
        assertThatThrownBy { UserRole.fromString("") }.isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun `anyRole returns all three roles`() {
        assertThat(UserRole.anyRole())
            .hasSize(3)
            .containsExactlyInAnyOrder(UserRole.USER, UserRole.MODERATOR, UserRole.ADMIN)
    }
}
