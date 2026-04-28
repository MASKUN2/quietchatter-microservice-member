package com.quietchatter.member.adaptor.`in`.web

import com.quietchatter.member.application.MemberService
import com.quietchatter.member.dto.UpdateProfileRequest
import com.quietchatter.member.infrastructure.AuthTokenService
import jakarta.servlet.http.HttpServletResponse
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/members/me")
class MeController(
    private val memberService: MemberService,
    private val authTokenService: AuthTokenService
) {

    @PutMapping("/profile")
    fun updateProfile(
        @RequestHeader("X-Member-Id") memberId: UUID,
        @Valid @RequestBody request: UpdateProfileRequest
    ): ResponseEntity<Unit> {
        memberService.updateNickname(memberId, request.nickname)
        return ResponseEntity.noContent().build()
    }

    @DeleteMapping
    fun withdraw(
        @RequestHeader("X-Member-Id") memberId: UUID,
        @CookieValue(name = "REFRESH_TOKEN", required = false) refreshToken: String?,
        response: HttpServletResponse
    ): ResponseEntity<Unit> {
        memberService.deactivate(memberId)
        
        if (refreshToken != null) {
            authTokenService.deleteRefreshTokenByValue(refreshToken)
        }
        authTokenService.expireTokenCookies(response)
        
        return ResponseEntity.noContent().build()
    }
}
