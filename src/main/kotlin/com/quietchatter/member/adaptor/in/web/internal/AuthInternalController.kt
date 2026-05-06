package com.quietchatter.member.adaptor.`in`.web.internal

import com.quietchatter.member.infrastructure.AuthTokenService
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException

data class RefreshResponse(val memberId: String)

@RestController
@RequestMapping("/internal/auth")
class AuthInternalController(
    private val authTokenService: AuthTokenService,
    @Value("\${INTERNAL_SECRET:default-internal-secret}") private val internalSecret: String
) {
    @PostMapping("/refresh")
    fun refresh(
        @RequestHeader("X-Internal-Secret") secret: String,
        @RequestHeader("X-Refresh-Token") refreshToken: String,
        servletResponse: HttpServletResponse
    ): ResponseEntity<RefreshResponse> {
        if (secret != internalSecret) throw ResponseStatusException(HttpStatus.FORBIDDEN)
        val result = authTokenService.rotateRefreshToken(refreshToken)
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        authTokenService.putRotatedTokensInCookies(servletResponse, result)
        return ResponseEntity.ok(RefreshResponse(result.memberId))
    }
}
