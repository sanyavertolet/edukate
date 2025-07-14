package io.github.sanyavertolet.edukate.auth.utils;

import org.springframework.security.web.server.util.matcher.OrServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.PathPatternParserServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher;

import java.util.List;

public class PublicEndpoints {
    private PublicEndpoints() {
        throw new IllegalStateException("Utility class");
    }

    private static final List<String> publicEndpoints = List.of(
            "/actuator/**",
            "/actuator/health/liveness",
            "/internal/**",
            "/api/v1/problems/**",
            "/api/v1/auth/*"
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
