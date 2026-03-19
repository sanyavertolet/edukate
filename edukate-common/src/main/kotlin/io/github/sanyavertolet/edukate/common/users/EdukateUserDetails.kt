package io.github.sanyavertolet.edukate.common.users

import lombok.AllArgsConstructor
import org.springframework.security.core.CredentialsContainer
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken

@AllArgsConstructor
class EdukateUserDetails(
    val id: String,
    val name: String,
    val roles: Set<UserRole> = emptySet(),
    val status: UserStatus,
    token: String
) : UserDetails, CredentialsContainer {
    constructor(userCredentials: UserCredentials) : this(
        requireNotNull(userCredentials.id) { "User id must not be null" },
        userCredentials.username,
        userCredentials.roles,
        userCredentials.status,
        userCredentials.encodedPassword
    )

    private var token: String = token

    fun toPreAuthenticatedAuthenticationToken() = PreAuthenticatedAuthenticationToken(this, null, authorities)

    override fun getAuthorities(): MutableCollection<out GrantedAuthority?> = roles.map { it.asGrantedAuthority() }
        .toMutableSet()

    override fun isEnabled(): Boolean = status == UserStatus.ACTIVE

    override fun getUsername(): String = name

    override fun getPassword(): String = token

    override fun toString(): String = "EdukateUserDetails(id=$id, name=$name, roles=$roles, status=$status)"

    override fun eraseCredentials() {
        token = ""
    }
}
