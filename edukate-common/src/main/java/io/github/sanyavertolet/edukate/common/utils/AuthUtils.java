package io.github.sanyavertolet.edukate.common.utils;

import io.github.sanyavertolet.edukate.common.users.EdukateUserDetails;
import lombok.experimental.UtilityClass;
import org.springframework.security.core.Authentication;
import reactor.core.publisher.Mono;

@UtilityClass
public class AuthUtils {

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
