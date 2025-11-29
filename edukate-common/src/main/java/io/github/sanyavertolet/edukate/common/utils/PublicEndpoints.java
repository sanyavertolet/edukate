package io.github.sanyavertolet.edukate.common.utils;

import lombok.experimental.UtilityClass;
import org.springframework.security.web.server.util.matcher.OrServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.PathPatternParserServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher;

import java.util.List;

@UtilityClass
public class PublicEndpoints {
    private static final List<String> publicEndpoints = List.of(
            "/actuator/**",
            "/internal/**",
            "/api/v1/problems/**",
            "/api/v1/auth/*",
            "/swagger/**",
            "/swagger-ui/**"
    );

    public static String[] asArray() {
        return publicEndpoints.toArray(new String[0]);
    }

    @SuppressWarnings("unused")
    public static List<String> asList() {
        return publicEndpoints;
    }

    public static ServerWebExchangeMatcher asMatcher() {
        List<ServerWebExchangeMatcher> publicMatchers = publicEndpoints.stream()
                .map(PathPatternParserServerWebExchangeMatcher::new)
                .map(it -> (ServerWebExchangeMatcher)it)
                .toList();
        return new OrServerWebExchangeMatcher(publicMatchers);
    }
}
