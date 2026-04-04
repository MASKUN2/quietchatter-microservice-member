package com.quietchatter.member.application.out

import com.quietchatter.member.domain.Member
import com.quietchatter.member.domain.OauthProvider
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface MemberRepository : JpaRepository<Member, UUID> {
    fun findByProviderAndProviderId(provider: OauthProvider, providerId: String): Member?
}
