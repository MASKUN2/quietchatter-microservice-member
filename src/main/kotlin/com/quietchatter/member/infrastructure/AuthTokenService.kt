package com.quietchatter.member.infrastructure

import com.quietchatter.member.dto.TokenRotationResult
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseCookie
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.Instant
import java.util.*
import javax.crypto.SecretKey

@Service
class AuthTokenService(
    @Value("\${jwt.secret-key}") rawKey: String,
    private val redisTemplate: StringRedisTemplate,
    private val cookieProperties: AppCookieProperties
) {
    private val secretKey: SecretKey = Keys.hmacShaKeyFor(rawKey.toByteArray(Charsets.UTF_8))
    private val jwtParser = Jwts.parser().verifyWith(secretKey).build()

    private val accessTokenLifetime = Duration.ofMinutes(30)
    private val refreshTokenLifetime = Duration.ofDays(30)
    private val registerTokenLifetime = Duration.ofHours(2)
    private val reactivationTokenLifetime = Duration.ofHours(2)

    private val accessTokenCookieName = "ACCESS_TOKEN"
    private val refreshTokenCookieName = "REFRESH_TOKEN"

    fun createNewAccessToken(memberId: UUID): String {
        val exp = Date.from(Instant.now().plus(accessTokenLifetime))
        return Jwts.builder()
            .subject(memberId.toString())
            .signWith(secretKey)
            .expiration(exp)
            .compact()
    }

    fun createAndSaveRefreshToken(memberId: UUID): String {
        val tokenId = UUID.randomUUID().toString()
        redisTemplate.opsForValue().set(
            "refresh_token:$tokenId",
            memberId.toString(),
            refreshTokenLifetime
        )

        val exp = Date.from(Instant.now().plus(refreshTokenLifetime))
        return Jwts.builder()
            .id(tokenId)
            .signWith(secretKey)
            .expiration(exp)
            .compact()
    }

    fun putTokensInCookies(response: HttpServletResponse, memberId: UUID) {
        val accessToken = createNewAccessToken(memberId)
        val refreshToken = createAndSaveRefreshToken(memberId)

        addCookie(response, accessTokenCookieName, accessToken, accessTokenLifetime)
        addCookie(response, refreshTokenCookieName, refreshToken, refreshTokenLifetime)
    }

    fun expireTokenCookies(response: HttpServletResponse) {
        addCookie(response, accessTokenCookieName, "", Duration.ZERO)
        addCookie(response, refreshTokenCookieName, "", Duration.ZERO)
    }

    private fun addCookie(response: HttpServletResponse, name: String, value: String, maxAge: Duration) {
        val cookie = ResponseCookie.from(name, value)
            .path("/")
            .httpOnly(true)
            .maxAge(maxAge)
            .secure(cookieProperties.secure)
            .sameSite(cookieProperties.sameSite)
            .domain(cookieProperties.domain)
            .build()
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString())
    }

    fun createRegisterToken(providerId: String): String {
        val exp = Date.from(Instant.now().plus(registerTokenLifetime))
        return Jwts.builder()
            .subject(providerId)
            .claim("purpose", "register")
            .signWith(secretKey)
            .expiration(exp)
            .compact()
    }

    fun parseRegisterToken(token: String): String? {
        return try {
            val claims = jwtParser.parseSignedClaims(token).payload
            if (claims["purpose"] == "register") {
                claims.subject
            } else null
        } catch (e: Exception) {
            null
        }
    }

    fun createReactivationToken(memberId: UUID): String {
        val exp = Date.from(Instant.now().plus(reactivationTokenLifetime))
        return Jwts.builder()
            .subject(memberId.toString())
            .claim("purpose", "reactivate")
            .signWith(secretKey)
            .expiration(exp)
            .compact()
    }

    fun parseReactivationToken(token: String): UUID? {
        return try {
            val claims = jwtParser.parseSignedClaims(token).payload
            if (claims["purpose"] == "reactivate") {
                UUID.fromString(claims.subject)
            } else null
        } catch (e: Exception) {
            null
        }
    }

    fun rotateRefreshToken(refreshTokenValue: String): TokenRotationResult? {
        return try {
            val claims = jwtParser.parseSignedClaims(refreshTokenValue).payload
            val tokenId = claims.id ?: return null
            val memberIdStr = redisTemplate.opsForValue().getAndDelete("refresh_token:$tokenId") ?: return null
            val memberId = UUID.fromString(memberIdStr)
            val newAccessToken = createNewAccessToken(memberId)
            val newRefreshToken = createAndSaveRefreshToken(memberId)
            TokenRotationResult(newAccessToken, newRefreshToken, memberIdStr)
        } catch (e: Exception) {
            null
        }
    }

    fun deleteRefreshTokenByValue(refreshToken: String) {
        try {
            val claims = jwtParser.parseSignedClaims(refreshToken).payload
            val tokenId = claims.id
            if (tokenId != null) {
                redisTemplate.delete("refresh_token:$tokenId")
            }
        } catch (e: Exception) {
            // Ignore if token is already invalid
        }
    }
}
