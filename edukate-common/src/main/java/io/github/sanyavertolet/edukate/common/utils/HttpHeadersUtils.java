package io.github.sanyavertolet.edukate.common.utils;


import io.github.sanyavertolet.edukate.common.users.EdukateUserDetails;
import io.github.sanyavertolet.edukate.common.users.UserRole;
import io.github.sanyavertolet.edukate.common.users.UserStatus;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;

import java.util.List;
import java.util.Objects;

@Slf4j
@UtilityClass
public class HttpHeadersUtils {
    public static String getSingleHeader(HttpHeaders headers, String headerName) {
        List<String> headerCandidates = headers.get(headerName);
        if (headerCandidates != null && headerCandidates.size() == 1) {
            return headerCandidates.getFirst();
        }
        log.trace("Header {} is not provided: skipping pre-authenticated edukate-user authentication", headerName);
        return null;
    }

    public static void populateHeaders(HttpHeaders httpHeaders, EdukateUserDetails edukateUserDetails) {
        Objects.requireNonNull(httpHeaders, "HttpHeaders must not be null");
        httpHeaders.set(AuthHeaders.AUTHORIZATION_ID.headerName, edukateUserDetails.getId());
        httpHeaders.set(AuthHeaders.AUTHORIZATION_NAME.headerName, edukateUserDetails.getUsername());
        httpHeaders.set(AuthHeaders.AUTHORIZATION_STATUS.headerName, edukateUserDetails.getStatus().toString());
        String rolesString = UserRole.toString(edukateUserDetails.getRoles());
        httpHeaders.set(AuthHeaders.AUTHORIZATION_ROLES.headerName, rolesString);
    }

    public static EdukateUserDetails toEdukateUserDetails(HttpHeaders headers) {
        String id = getSingleHeader(headers, AuthHeaders.AUTHORIZATION_ID.headerName);
        String name = getSingleHeader(headers, AuthHeaders.AUTHORIZATION_NAME.headerName);
        String roles = getSingleHeader(headers, AuthHeaders.AUTHORIZATION_ROLES.headerName);
        String status = getSingleHeader(headers, AuthHeaders.AUTHORIZATION_STATUS.headerName);

        if (id == null || name == null || roles == null || status == null) {
            return null;
        }

        return new EdukateUserDetails(id, name, UserRole.fromString(roles), UserStatus.valueOf(status), null);
    }
}
