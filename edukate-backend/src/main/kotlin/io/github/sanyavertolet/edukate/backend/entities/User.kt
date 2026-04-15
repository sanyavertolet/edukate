package io.github.sanyavertolet.edukate.backend.entities

import io.github.sanyavertolet.edukate.common.users.UserCredentials
import io.github.sanyavertolet.edukate.common.users.UserRole
import io.github.sanyavertolet.edukate.common.users.UserStatus
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document

@Document("users")
data class User(
    @field:Id val id: String? = null,
    @field:Indexed(unique = true) val name: String,
    val email: String? = null,
    val token: String,
    val roles: Set<UserRole>,
    val status: UserStatus,
) {
    fun toCredentials() = UserCredentials(id, name, token, email, roles, status)

    override fun toString(): String = "User(id=$id, name=$name, email=$email, roles=$roles, status=$status)"

    companion object {
        @JvmStatic
        fun newFromCredentials(credentials: UserCredentials): User =
            User(
                name = credentials.username,
                email = credentials.email,
                token = credentials.encodedPassword,
                roles = setOf(UserRole.USER),
                status = UserStatus.PENDING,
            )
    }
}
