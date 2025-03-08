package io.github.sanyavertolet.edukate.auth.utils;


import io.github.sanyavertolet.edukate.auth.EdukateUserDetails;
import io.github.sanyavertolet.edukate.common.Role;
import io.github.sanyavertolet.edukate.common.UserStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;

import java.util.List;

import static io.github.sanyavertolet.edukate.common.AuthHeaders.*;

@Slf4j
public class AuthUtils {
    private static final String BEARER_PREFIX = "Bearer ";

    private AuthUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static EdukateUserDetails toEdukateUserDetails(HttpHeaders headers) {
        String id = getSingleHeader(headers, AUTHORIZATION_ID.headerName);
        String name = getSingleHeader(headers, AUTHORIZATION_NAME.headerName);
        String roles = getSingleHeader(headers, AUTHORIZATION_ROLES.headerName);
        String status = getSingleHeader(headers, AUTHORIZATION_STATUS.headerName);

        return new EdukateUserDetails(id, name, Role.fromString(roles), UserStatus.valueOf(status), null);
    }

    private static String getSingleHeader(HttpHeaders headers, String headerName) {
        List<String> headerCandidates = headers.get(headerName);
        if (headerCandidates != null && headerCandidates.size() == 1) {
            return headerCandidates.getFirst();
        }
        log.debug("Header {} is not provided: skipping pre-authenticated edukate-user authentication", headerName);
        return null;
    }

    public static String getTokenFromHeaders(HttpHeaders headers) {
        String authorizationHeader = getSingleHeader(headers, HttpHeaders.AUTHORIZATION);
        if (authorizationHeader == null || !authorizationHeader.startsWith(BEARER_PREFIX)) {
            return null;
        }
        return authorizationHeader.substring(BEARER_PREFIX.length());
    }
}
