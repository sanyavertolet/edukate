package io.github.sanyavertolet.edukate.common.utils

import io.github.sanyavertolet.edukate.common.users.EdukateUserDetails
import io.github.sanyavertolet.edukate.common.users.UserRole
import io.github.sanyavertolet.edukate.common.users.UserStatus
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders

fun populateHeaders(httpHeaders: HttpHeaders?, edukateUserDetails: EdukateUserDetails) {
    requireNotNull(httpHeaders) { "HttpHeaders must not be null" }
        .apply {
            set(AuthHeaders.AUTHORIZATION_ID.headerName, edukateUserDetails.id)
            set(AuthHeaders.AUTHORIZATION_NAME.headerName, edukateUserDetails.username)
            set(AuthHeaders.AUTHORIZATION_STATUS.headerName, edukateUserDetails.status.toString())
            set(AuthHeaders.AUTHORIZATION_ROLES.headerName, UserRole.listToString(edukateUserDetails.roles))
        }
}

private fun getLastHeaderOrNull(headers: HttpHeaders, headerName: String): String? {
    val headerCandidates = headers[headerName] ?: return null
    return if (headerCandidates.isNotEmpty()) {
        headerCandidates.last()
    } else {
        logger.trace("Header {} is not provided: skipping pre-authenticated edukate-user authentication", headerName)
        null
    }
}

fun HttpHeaders.toEdukateUserDetails(): EdukateUserDetails? {
    val id = getLastHeaderOrNull(this, AuthHeaders.AUTHORIZATION_ID.headerName) ?: return null
    val name = getLastHeaderOrNull(this, AuthHeaders.AUTHORIZATION_NAME.headerName) ?: return null
    val rolesString = getLastHeaderOrNull(this, AuthHeaders.AUTHORIZATION_ROLES.headerName) ?: return null
    val statusString = getLastHeaderOrNull(this, AuthHeaders.AUTHORIZATION_STATUS.headerName) ?: return null

    return EdukateUserDetails(id, name, UserRole.fromString(rolesString), UserStatus.valueOf(statusString), "")
}

private val logger = LoggerFactory.getLogger("HttpHeadersUtils")
