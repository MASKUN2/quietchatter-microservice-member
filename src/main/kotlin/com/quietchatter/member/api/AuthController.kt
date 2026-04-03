package com.quietchatter.member.api

import com.quietchatter.member.application.MemberService
import com.quietchatter.member.dto.NaverLoginRequest
import com.quietchatter.member.dto.NaverLoginResponse
import com.quietchatter.member.dto.SignupRequest
import com.quietchatter.member.infrastructure.AuthTokenService
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/members")
class AuthController(
    private val memberService: MemberService,
    private val authTokenService: AuthTokenService
) {

    @PostMapping("/login/naver")
    fun loginWithNaver(
        @RequestBody request: NaverLoginRequest,
        response: HttpServletResponse
    ): ResponseEntity<NaverLoginResponse> {
        val loginResponse = memberService.loginWithNaver(request, response)
        return ResponseEntity.ok(loginResponse)
    }

    @PostMapping("/signup")
    fun signup(
        @RequestBody request: SignupRequest,
        response: HttpServletResponse
    ): ResponseEntity<Unit> {
        val member = memberService.signup(request)
        authTokenService.putTokensInCookies(response, member.id!!)
        return ResponseEntity.ok().build()
    }
}
