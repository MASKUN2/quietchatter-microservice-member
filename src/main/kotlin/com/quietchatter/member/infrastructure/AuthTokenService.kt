package com.quietchatter.member.infrastructure

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
    private val secretKey: SecretKey = Keys.hmacShaKeyFor(rawKey.toByteArray())
    private val jwtParser = Jwts.parser().verifyWith(secretKey).build()

    private val accessTokenLifetime = Duration.ofMinutes(30)
    private val refreshTokenLifetime = Duration.ofDays(30)
    private val registerTokenLifetime = Duration.ofHours(2)

    private val accessTokenCookieName = "access_token"
    private val refreshTokenCookieName = "refresh_token"

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
}
