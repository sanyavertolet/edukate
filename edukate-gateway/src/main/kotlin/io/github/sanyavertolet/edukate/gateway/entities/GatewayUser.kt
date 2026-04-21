package io.github.sanyavertolet.edukate.gateway.entities

import io.github.sanyavertolet.edukate.common.users.UserCredentials
import io.github.sanyavertolet.edukate.common.users.UserRole
import io.github.sanyavertolet.edukate.common.users.UserStatus
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("users")
data class GatewayUser(
    @Id val id: Long? = null,
    val name: String,
    val email: String? = null,
    val token: String,
    val roles: Set<UserRole>,
    val status: UserStatus,
) {
    fun toCredentials() = UserCredentials(id, name, token, email, roles, status)
}
