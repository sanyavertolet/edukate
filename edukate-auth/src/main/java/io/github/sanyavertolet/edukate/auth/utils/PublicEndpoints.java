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
            "/api/v1/problems/**",
            "/actuator/health",
            "/actuator/info",
            "/auth/**"
    );

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
