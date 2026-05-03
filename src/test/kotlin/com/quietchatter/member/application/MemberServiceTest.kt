package com.quietchatter.member.application

import com.fasterxml.jackson.databind.ObjectMapper
import com.quietchatter.member.adaptor.out.external.NaverClient
import com.quietchatter.member.adaptor.out.outbox.OutboxEventRepository
import com.quietchatter.member.application.out.MemberRepository
import com.quietchatter.member.domain.Member
import com.quietchatter.member.domain.OauthProvider
import com.quietchatter.member.infrastructure.AuthTokenService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.util.UUID

class MemberServiceTest {

    private val memberRepository: MemberRepository = mock()
    private val outboxEventRepository: OutboxEventRepository = mock()
    private val naverClient: NaverClient = mock()
    private val authTokenService: AuthTokenService = mock()
    private val randomNickNameSupplier: RandomNickNameSupplier = mock()
    private val objectMapper: ObjectMapper = ObjectMapper()
    
    private val memberService = MemberService(
        memberRepository,
        outboxEventRepository,
        naverClient,
        authTokenService,
        randomNickNameSupplier,
        objectMapper
    )

    @Test
    fun `should signup new member`() {
        val nickname = "testNick"
        val registerToken = "token"
        val providerId = "naver123"
        val expectedMemberId = UUID.randomUUID()
        
        whenever(authTokenService.parseRegisterToken(registerToken)).thenReturn(providerId)
        whenever(memberRepository.findByProviderAndProviderId(OauthProvider.NAVER, providerId)).thenReturn(null)
        whenever(memberRepository.save(any<Member>())).thenAnswer { 
            val member = it.getArgument<Member>(0)
            val memberField = Member::class.java.superclass.getDeclaredField("id")
            memberField.isAccessible = true
            memberField.set(member, expectedMemberId)
            member
        }

        val member = memberService.signup(nickname, registerToken)

        assertEquals(nickname, member.nickname)
        assertEquals(providerId, member.providerId)
    }
}
