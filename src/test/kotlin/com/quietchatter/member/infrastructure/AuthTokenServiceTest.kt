package com.quietchatter.member.infrastructure

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.core.ValueOperations
import java.time.Duration
import java.util.UUID
import javax.crypto.SecretKey

class AuthTokenServiceTest {

    private val redisTemplate: StringRedisTemplate = mock()
    private val valueOperations: ValueOperations<String, String> = mock()
    private val cookieProperties = AppCookieProperties(domain = null, secure = false, sameSite = "Lax")
    private val secretKey = "veryveryveryveryLongSecretKeyForLocalDevelopment"

    private lateinit var authTokenService: AuthTokenService

    @BeforeEach
    fun setUp() {
        whenever(redisTemplate.opsForValue()).thenReturn(valueOperations)
        authTokenService = AuthTokenService(secretKey, redisTemplate, cookieProperties)
    }

    @Test
    fun `rotateRefreshToken returns result when refresh token is valid and in Redis`() {
        val memberId = UUID.randomUUID()
        val refreshToken = authTokenService.createAndSaveRefreshToken(memberId)

        val tokenId = io.jsonwebtoken.Jwts.parser()
            .verifyWith(io.jsonwebtoken.security.Keys.hmacShaKeyFor(secretKey.toByteArray(Charsets.UTF_8)))
            .build()
            .parseSignedClaims(refreshToken).payload.id!!

        whenever(valueOperations.getAndDelete("refresh_token:$tokenId")).thenReturn(memberId.toString())

        val result = authTokenService.rotateRefreshToken(refreshToken)

        assertNotNull(result)
        assertEquals(memberId.toString(), result!!.memberId)
        assertNotNull(result.accessToken)
        assertNotNull(result.refreshToken)
    }

    @Test
    fun `rotateRefreshToken returns null when Redis entry is missing (session expired)`() {
        val memberId = UUID.randomUUID()
        val refreshToken = authTokenService.createAndSaveRefreshToken(memberId)

        val tokenId = io.jsonwebtoken.Jwts.parser()
            .verifyWith(io.jsonwebtoken.security.Keys.hmacShaKeyFor(secretKey.toByteArray(Charsets.UTF_8)))
            .build()
            .parseSignedClaims(refreshToken).payload.id!!

        whenever(valueOperations.getAndDelete("refresh_token:$tokenId")).thenReturn(null)

        val result = authTokenService.rotateRefreshToken(refreshToken)

        assertNull(result)
    }

    @Test
    fun `rotateRefreshToken returns null when token is invalid`() {
        val result = authTokenService.rotateRefreshToken("invalid-token-value")
        assertNull(result)
    }
}
