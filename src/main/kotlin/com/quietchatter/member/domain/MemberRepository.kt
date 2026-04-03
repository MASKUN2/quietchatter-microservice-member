package com.quietchatter.member.domain

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface MemberRepository : JpaRepository<Member, UUID> {
    fun findByProviderAndProviderId(provider: OauthProvider, providerId: String): Member?
}
