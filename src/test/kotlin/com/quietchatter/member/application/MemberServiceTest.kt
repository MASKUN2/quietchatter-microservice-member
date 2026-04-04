package com.quietchatter.member.application

import com.quietchatter.member.adaptor.out.external.NaverClient
import com.quietchatter.member.adaptor.out.external.TalkServiceClient
import com.quietchatter.member.application.out.MemberRepository
import com.quietchatter.member.domain.Member
import com.quietchatter.member.domain.OauthProvider
import com.quietchatter.member.infrastructure.AuthTokenService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class MemberServiceTest {

    private val memberRepository: MemberRepository = mock()
    private val naverClient: NaverClient = mock()
    private val authTokenService: AuthTokenService = mock()
    private val randomNickNameSupplier: RandomNickNameSupplier = mock()
    private val talkServiceClient: TalkServiceClient = mock()
    
    private val memberService = MemberService(
        memberRepository,
        naverClient,
        authTokenService,
        randomNickNameSupplier,
        talkServiceClient
    )

    @Test
    fun `should signup new member`() {
        val nickname = "testNick"
        val registerToken = "token"
        val providerId = "naver123"
        
        whenever(authTokenService.parseRegisterToken(registerToken)).thenReturn(providerId)
        whenever(memberRepository.findByProviderAndProviderId(OauthProvider.NAVER, providerId)).thenReturn(null)
        whenever(memberRepository.save(any<Member>())).thenAnswer { it.getArgument<Member>(0) }

        val member = memberService.signup(nickname, registerToken)

        assertEquals(nickname, member.nickname)
        assertEquals(providerId, member.providerId)
    }
}
