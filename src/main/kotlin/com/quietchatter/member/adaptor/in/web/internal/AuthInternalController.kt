package com.quietchatter.member.adaptor.`in`.web.internal

import com.quietchatter.member.dto.TokenRotationResult
import com.quietchatter.member.infrastructure.AuthTokenService
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("/internal/auth")
class AuthInternalController(
    private val authTokenService: AuthTokenService,
    @Value("\${INTERNAL_SECRET:default-internal-secret}") private val internalSecret: String
) {
    @PostMapping("/refresh")
    fun refresh(
        @RequestHeader("X-Internal-Secret") secret: String,
        @RequestHeader("X-Refresh-Token") refreshToken: String
    ): ResponseEntity<TokenRotationResult> {
        if (secret != internalSecret) throw ResponseStatusException(HttpStatus.FORBIDDEN)
        val result = authTokenService.rotateRefreshToken(refreshToken)
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        return ResponseEntity.ok(result)
    }
}
