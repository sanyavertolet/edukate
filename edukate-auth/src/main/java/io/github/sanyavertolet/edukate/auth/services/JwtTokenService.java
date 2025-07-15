package io.github.sanyavertolet.edukate.auth.services;

import io.github.sanyavertolet.edukate.auth.EdukateUserDetails;
import io.github.sanyavertolet.edukate.common.Role;
import io.github.sanyavertolet.edukate.common.UserStatus;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class JwtTokenService {
    @Value("${auth.jwt.secret}")
    private String secretKey;

    @Getter
    @Value("${auth.jwt.expirationSeconds}")
    private long expirationTimeSeconds;

    private SecretKey key;

    @PostConstruct
    void init() {
        key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    private Date getExpirationDate(Date now) {
        return new Date(now.getTime() + TimeUnit.SECONDS.toMillis(expirationTimeSeconds));
    }

    public String generateToken(EdukateUserDetails userDetails) {
        Date now = new Date();

        return Jwts.builder()
                .subject(userDetails.getUsername())
                .issuedAt(now)
                .claim("roles", Role.toString(userDetails.getRoles()))
                .claim("status", userDetails.getStatus().toString())
                .expiration(getExpirationDate(now))
                .signWith(key)
                .compact();
    }

    public EdukateUserDetails getUserDetailsFromToken(String token) {
        Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();

        if (claims.getExpiration().before(new Date())) {
            return null;
        }

        log.debug("Token recognized, subject: {}", claims.getSubject());
        return new EdukateUserDetails(
                claims.getSubject(),
                Role.fromString(claims.get("roles", String.class)),
                UserStatus.valueOf(claims.get("status", String.class)),
                token
        );
    }
}
