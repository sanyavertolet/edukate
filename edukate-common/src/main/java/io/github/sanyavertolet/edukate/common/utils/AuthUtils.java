package io.github.sanyavertolet.edukate.common.utils;

import io.github.sanyavertolet.edukate.common.EdukateUserDetails;
import org.springframework.security.core.Authentication;
import reactor.core.publisher.Mono;

public class AuthUtils {
    private AuthUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static String id(Authentication authentication) {
        if (authentication == null) {
            return null;
        }
        EdukateUserDetails userDetails = (EdukateUserDetails) authentication.getPrincipal();
        return userDetails.getId();
    }

    public static Mono<String> monoId(Authentication authentication) {
        return Mono.justOrEmpty(id(authentication));
    }
}
