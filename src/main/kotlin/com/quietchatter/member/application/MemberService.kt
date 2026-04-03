package com.quietchatter.member.application

import com.quietchatter.member.domain.Member
import com.quietchatter.member.domain.MemberRepository
import com.quietchatter.member.domain.OauthProvider
import com.quietchatter.member.domain.Status
import com.quietchatter.member.dto.NaverLoginRequest
import com.quietchatter.member.dto.NaverLoginResponse
import com.quietchatter.member.dto.SignupRequest
import com.quietchatter.member.infrastructure.AuthTokenService
import com.quietchatter.member.infrastructure.NaverClient
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class MemberService(
    private val memberRepository: MemberRepository,
    private val naverClient: NaverClient,
    private val authTokenService: AuthTokenService
) {

    @Transactional
    fun loginWithNaver(request: NaverLoginRequest, response: HttpServletResponse): NaverLoginResponse {
        val tokenResponse = naverClient.getAccessToken(request.code, request.state)
            ?: throw RuntimeException("Failed to get Naver access token")
        
        val profileResponse = naverClient.getProfile(tokenResponse.accessToken!!)
            ?: throw RuntimeException("Failed to get Naver profile")
        
        val providerId = profileResponse.response!!.id
        val member = memberRepository.findByProviderAndProviderId(OauthProvider.NAVER, providerId)

        return if (member != null) {
            if (member.status == Status.DEACTIVATED) {
                throw RuntimeException("Member is deactivated")
            }
            // Issue tokens immediately for registered members
            authTokenService.putTokensInCookies(response, member.id!!)
            NaverLoginResponse.registered()
        } else {
            val registerToken = authTokenService.createRegisterToken(providerId)
            val tempNickname = "Member_${UUID.randomUUID().toString().substring(0, 8)}"
            NaverLoginResponse.notRegistered(registerToken, tempNickname)
        }
    }

    @Transactional
    fun signup(request: SignupRequest): Member {
        val providerId = authTokenService.parseRegisterToken(request.registerToken)
            ?: throw IllegalArgumentException("Invalid register token")

        if (memberRepository.findByProviderAndProviderId(OauthProvider.NAVER, providerId) != null) {
            throw IllegalArgumentException("Already registered member")
        }

        val member = Member.newNaverMember(providerId, request.nickname)
        return memberRepository.save(member)
    }

    fun findByProviderId(providerId: String): Member? {
        return memberRepository.findByProviderAndProviderId(OauthProvider.NAVER, providerId)
    }
}
