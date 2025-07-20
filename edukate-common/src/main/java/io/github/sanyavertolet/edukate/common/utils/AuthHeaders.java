package io.github.sanyavertolet.edukate.common.utils;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum AuthHeaders {
    AUTHORIZATION_NAME("X-Authorization-Name"),
    AUTHORIZATION_ROLES("X-Authorization-Roles"),
    AUTHORIZATION_STATUS("X-Authorization-Status"),
    AUTHORIZATION_ID("X-Authorization-Id"),
    ;

    public final String headerName;
}
