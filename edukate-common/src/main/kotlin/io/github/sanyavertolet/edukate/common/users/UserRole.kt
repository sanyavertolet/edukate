package io.github.sanyavertolet.edukate.common.users

import org.springframework.security.core.authority.SimpleGrantedAuthority
import java.util.EnumSet

enum class UserRole {
    USER,
    MODERATOR,
    ADMIN;

    fun asSpringSecurityRole(): String = ROLE_PREFIX + name

    fun asGrantedAuthority() = SimpleGrantedAuthority(asSpringSecurityRole())

    companion object {
        private const val ROLE_PREFIX = "ROLE_"

        @JvmStatic fun listToString(roles: Set<UserRole>): String = roles.joinToString(",") { it.name }

        @JvmStatic
        fun fromString(rolesString: String?): Set<UserRole> =
            rolesString?.split(",")?.map { UserRole.valueOf(it.trim()) }?.toSet() ?: EnumSet.noneOf(UserRole::class.java)

        @JvmStatic fun anyRole(): Set<UserRole> = EnumSet.allOf(UserRole::class.java)
    }
}
