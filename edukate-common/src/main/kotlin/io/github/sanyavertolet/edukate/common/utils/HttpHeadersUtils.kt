package io.github.sanyavertolet.edukate.common.utils

import io.github.sanyavertolet.edukate.common.users.EdukateUserDetails
import io.github.sanyavertolet.edukate.common.users.UserRole
import io.github.sanyavertolet.edukate.common.users.UserStatus
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders

fun populateHeaders(httpHeaders: HttpHeaders, edukateUserDetails: EdukateUserDetails) {
    httpHeaders.apply {
        set(AuthHeaders.AUTHORIZATION_ID.headerName, edukateUserDetails.id.toString())
        set(AuthHeaders.AUTHORIZATION_NAME.headerName, edukateUserDetails.username)
        set(AuthHeaders.AUTHORIZATION_STATUS.headerName, edukateUserDetails.status.toString())
        set(AuthHeaders.AUTHORIZATION_ROLES.headerName, UserRole.listToString(edukateUserDetails.roles))
    }
}

fun HttpHeaders.toEdukateUserDetails(): EdukateUserDetails? {
    val id = get(AuthHeaders.AUTHORIZATION_ID.headerName)?.lastOrNull()
    val name = get(AuthHeaders.AUTHORIZATION_NAME.headerName)?.lastOrNull()
    val rolesString = get(AuthHeaders.AUTHORIZATION_ROLES.headerName)?.lastOrNull()
    val statusString = get(AuthHeaders.AUTHORIZATION_STATUS.headerName)?.lastOrNull()

    @Suppress("ComplexCondition")
    if (id == null || name == null || rolesString == null || statusString == null) {
        logger.trace("Authentication headers are is not provided: skipping pre-authenticated edukate user")
        return null
    }

    val numericId = id.toLongOrNull() ?: return null
    return EdukateUserDetails(numericId, name, UserRole.fromString(rolesString), UserStatus.valueOf(statusString), "")
}

private val logger = LoggerFactory.getLogger("HttpHeadersUtils")
