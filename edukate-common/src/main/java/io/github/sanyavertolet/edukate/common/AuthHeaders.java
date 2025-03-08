package io.github.sanyavertolet.edukate.common;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum AuthHeaders {
    AUTHORIZATION_ID("X-Authorization-Id"),
    AUTHORIZATION_NAME("X-Authorization-Name"),
    AUTHORIZATION_ROLES("X-Authorization-Roles"),
    AUTHORIZATION_STATUS("X-Authorization-Status"),
    ;

    public final String headerName;
}
