package io.github.sanyavertolet.edukate.auth.services

import io.github.sanyavertolet.edukate.common.users.EdukateUserDetails
import io.github.sanyavertolet.edukate.common.users.UserRole
import io.github.sanyavertolet.edukate.common.users.UserStatus
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import java.nio.charset.StandardCharsets
import java.util.Date
import java.util.concurrent.TimeUnit
import javax.crypto.SecretKey
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class JwtTokenService(
    @param:Value($$"${auth.jwt.secret}") private val secretKey: String,
    @param:Value($$"${auth.jwt.expirationSeconds}") private val expirationTimeSeconds: Long,
) {
    private val log = LoggerFactory.getLogger(JwtTokenService::class.java)
    private val key: SecretKey = Keys.hmacShaKeyFor(secretKey.toByteArray(StandardCharsets.UTF_8))

    private fun getExpirationDate(now: Date) = Date(now.time + TimeUnit.SECONDS.toMillis(expirationTimeSeconds))

    fun generateToken(userDetails: EdukateUserDetails): String {
        val now = Date()
        return Jwts.builder()
            .subject(userDetails.id.toString())
            .issuedAt(now)
            .claim("name", userDetails.username)
            .claim("roles", UserRole.listToString(userDetails.roles))
            .claim("status", userDetails.status.toString())
            .expiration(getExpirationDate(now))
            .signWith(key)
            .compact()
    }

    fun getUserDetailsFromToken(token: String): EdukateUserDetails? {
        val claims =
            try {
                Jwts.parser().verifyWith(key).build().parseSignedClaims(token).payload
            } catch (_: ExpiredJwtException) {
                return null
            }

        log.debug("Token recognized, subject: {} ({})", claims.subject, claims["name", String::class.java])
        val userId = claims.subject.toLongOrNull() ?: return null
        return EdukateUserDetails(
            userId,
            claims["name", String::class.java],
            UserRole.fromString(claims["roles", String::class.java]),
            UserStatus.valueOf(claims["status", String::class.java]),
            token,
        )
    }
}
