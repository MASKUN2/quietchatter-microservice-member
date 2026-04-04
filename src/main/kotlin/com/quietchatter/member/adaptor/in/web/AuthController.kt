package com.quietchatter.member.adaptor.`in`.web

import com.quietchatter.member.application.MemberDeactivatedException
import com.quietchatter.member.application.MemberService
import com.quietchatter.member.dto.*
import com.quietchatter.member.infrastructure.AuthTokenService
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.net.URI
import java.util.*

@RestController
@RequestMapping("/v1/auth")
class AuthController(
    private val memberService: MemberService,
    private val authTokenService: AuthTokenService
) {

    @PostMapping("/login/naver")
    fun loginWithNaver(
        @RequestBody request: NaverLoginRequest,
        response: HttpServletResponse
    ): ResponseEntity<NaverLoginResponse> {
        return try {
            val loginResponse = memberService.loginWithNaver(request, response)
            ResponseEntity.ok(loginResponse)
        } catch (e: MemberDeactivatedException) {
            val problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, e.message ?: "")
            problemDetail.type = URI.create("/errors/member-deactivated")
            problemDetail.setProperty("reactivationToken", e.reactivationToken)
            throw DeactivatedMemberException(problemDetail)
        }
    }

    @PostMapping("/signup")
    fun signup(
        @RequestHeader(HttpHeaders.AUTHORIZATION) authHeader: String,
        @RequestBody request: SignupRequest,
        response: HttpServletResponse
    ): ResponseEntity<Unit> {
        val registerToken = if (authHeader.startsWith("Bearer ")) authHeader.substring(7) else authHeader
        val member = memberService.signup(request.nickname, registerToken)
        authTokenService.putTokensInCookies(response, member.id!!)
        return ResponseEntity.status(HttpStatus.CREATED).build()
    }

    @PostMapping("/logout")
    fun logout(
        @CookieValue(name = "REFRESH_TOKEN", required = false) refreshToken: String?,
        response: HttpServletResponse
    ): ResponseEntity<Unit> {
        if (refreshToken != null) {
            authTokenService.deleteRefreshTokenByValue(refreshToken)
        }
        authTokenService.expireTokenCookies(response)
        return ResponseEntity.noContent().build()
    }

    @PostMapping("/reactivate")
    fun reactivate(
        @RequestHeader(HttpHeaders.AUTHORIZATION) authHeader: String,
        response: HttpServletResponse
    ): ResponseEntity<Unit> {
        val reactivationToken = if (authHeader.startsWith("Bearer ")) authHeader.substring(7) else authHeader
        memberService.reactivate(reactivationToken, response)
        return ResponseEntity.ok().build()
    }

    @GetMapping("/me")
    fun me(
        @RequestHeader(name = "X-Member-Id", required = false) memberIdStr: String?
    ): ResponseEntity<AuthMeResponse> {
        if (memberIdStr == null) {
            return ResponseEntity.ok(AuthMeResponse(isLoggedIn = false, role = "anonymous", nickname = "anonymous"))
        }

        val memberId = UUID.fromString(memberIdStr)
        val member = memberService.findById(memberId)
            ?: return ResponseEntity.ok(AuthMeResponse(isLoggedIn = false, role = "anonymous", nickname = "anonymous"))

        return ResponseEntity.ok(
            AuthMeResponse(
                isLoggedIn = true,
                id = member.id,
                nickname = member.nickname,
                role = member.role.name
            )
        )
    }

    @ExceptionHandler(DeactivatedMemberException::class)
    fun handleDeactivatedMember(e: DeactivatedMemberException): ResponseEntity<ProblemDetail> {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.problemDetail)
    }
}

class DeactivatedMemberException(val problemDetail: ProblemDetail) : RuntimeException()
