package io.github.sanyavertolet.edukate.common.utils

enum class AuthHeaders(val headerName: String) {
    AUTHORIZATION_NAME("X-Authorization-Name"),
    AUTHORIZATION_ROLES("X-Authorization-Roles"),
    AUTHORIZATION_STATUS("X-Authorization-Status"),
    AUTHORIZATION_ID("X-Authorization-Id"),
    ;
}
