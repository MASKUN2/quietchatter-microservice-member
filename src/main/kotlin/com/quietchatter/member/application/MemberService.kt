package com.quietchatter.member.application

import com.quietchatter.member.adaptor.out.external.NaverClient
import com.quietchatter.member.adaptor.out.external.TalkServiceClient
import com.quietchatter.member.adaptor.out.outbox.OutboxEvent
import com.quietchatter.member.adaptor.out.outbox.OutboxEventRepository
import com.quietchatter.member.application.out.MemberRepository
import com.quietchatter.member.domain.Member
import com.quietchatter.member.domain.OauthProvider
import com.quietchatter.member.domain.Status
import com.quietchatter.member.dto.NaverLoginRequest
import com.quietchatter.member.dto.NaverLoginResponse
import com.quietchatter.member.infrastructure.AuthTokenService
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class MemberService(
    private val memberRepository: MemberRepository,
    private val outboxEventRepository: OutboxEventRepository,
    private val naverClient: NaverClient,
    private val authTokenService: AuthTokenService,
    private val randomNickNameSupplier: RandomNickNameSupplier,
    private val talkServiceClient: TalkServiceClient
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
                val reactivationToken = authTokenService.createReactivationToken(member.id!!)
                throw MemberDeactivatedException(reactivationToken)
            }
            // Issue tokens immediately for registered members
            authTokenService.putTokensInCookies(response, member.id!!)
            NaverLoginResponse.registered()
        } else {
            val registerToken = authTokenService.createRegisterToken(providerId)
            val tempNickname = randomNickNameSupplier.get()
            NaverLoginResponse.notRegistered(registerToken, tempNickname)
        }
    }

    @Transactional
    fun signup(nickname: String, registerToken: String): Member {
        val providerId = authTokenService.parseRegisterToken(registerToken)
            ?: throw IllegalArgumentException("Invalid register token")

        if (memberRepository.findByProviderAndProviderId(OauthProvider.NAVER, providerId) != null) {
            throw IllegalArgumentException("Already registered member")
        }

        val member = Member.newNaverMember(providerId, nickname)
        val savedMember = memberRepository.save(member)

        val eventPayload = """{"memberId": "${savedMember.id}", "nickname": "${savedMember.nickname}"}"""
        val outboxEvent = OutboxEvent(
            aggregateType = "Member",
            aggregateId = savedMember.id.toString(),
            type = "MemberRegisteredEvent",
            payload = eventPayload
        )
        outboxEventRepository.save(outboxEvent)

        return savedMember
    }

    @Transactional
    fun reactivate(reactivationToken: String, response: HttpServletResponse) {
        val memberId = authTokenService.parseReactivationToken(reactivationToken)
            ?: throw IllegalArgumentException("Invalid reactivation token")
        
        val member = memberRepository.findById(memberId).orElseThrow { 
            MemberNotFoundException("Member not found") 
        }
        
        member.activate()
        authTokenService.putTokensInCookies(response, member.id!!)
    }

    @Transactional(readOnly = true)
    fun findById(id: UUID): Member? {
        return memberRepository.findById(id).orElse(null)
    }

    @Transactional
    fun updateNickname(id: UUID, nickname: String) {
        val member = memberRepository.findById(id).orElseThrow { 
            MemberNotFoundException("Member not found") 
        }
        member.updateNickname(nickname)
    }

    @Transactional
    fun deactivate(id: UUID) {
        val member = memberRepository.findById(id).orElseThrow { 
            MemberNotFoundException("Member not found") 
        }
        member.deactivate()
        talkServiceClient.hideAllByMember(id)
    }
}
