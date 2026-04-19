package io.github.sanyavertolet.edukate.common.users

data class UserCredentials(
    val id: Long?,
    val username: String,
    val encodedPassword: String,
    val email: String? = null,
    val roles: Set<UserRole>,
    val status: UserStatus,
) {
    override fun toString(): String =
        "UserCredentials(id=$id, username='$username', email='$email', roles=$roles, status=$status)"

    companion object {
        @JvmStatic
        fun newUser(username: String, encodedPassword: String, email: String) =
            UserCredentials(null, username, encodedPassword, email, setOf(UserRole.USER), UserStatus.PENDING)
    }
}
