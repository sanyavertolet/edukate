package io.github.sanyavertolet.edukate.auth.services;

import io.github.sanyavertolet.edukate.auth.EdukateUserDetails;
import io.github.sanyavertolet.edukate.common.Role;
import io.github.sanyavertolet.edukate.common.UserStatus;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Set;

@Slf4j
@Service
public class JwtTokenService {
    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long expirationTimeMillis;

    private SecretKey key;

    @PostConstruct
    void init() {
        key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    private Date getExpirationDate(Date now) {
        return new Date(now.getTime() + expirationTimeMillis);
    }

    public String generateToken(EdukateUserDetails userDetails) {
        Date now = new Date();

        return Jwts.builder()
                .subject(userDetails.getUsername())
                .issuedAt(now)
                .claim("roles", userDetails.getRoles())
                .claim("status", userDetails.getStatus())
                .expiration(getExpirationDate(now))
                .signWith(key)
                .compact();
    }

    public EdukateUserDetails getUserDetailsFromToken(String token) {
        Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
        boolean isValid = claims.getExpiration().after(new Date());
        if (!isValid) {
            return null;
        }
        @SuppressWarnings("unchecked")
        Set<Role> roles = claims.get("roles", Set.class);
        UserStatus status = UserStatus.valueOf(claims.get("status", String.class));
        return new EdukateUserDetails(claims.getSubject(), roles, status, token);
    }
}
