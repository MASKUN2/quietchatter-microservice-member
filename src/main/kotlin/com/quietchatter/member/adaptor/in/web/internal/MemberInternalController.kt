package com.quietchatter.member.adaptor.`in`.web.internal

import com.quietchatter.member.application.MemberService
import com.quietchatter.member.dto.InternalMemberResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import java.util.UUID

@RestController
@RequestMapping("/internal/api/members")
class MemberInternalController(
    private val memberService: MemberService,
    @Value("\${INTERNAL_SECRET:default-internal-secret}") private val internalSecret: String
) {
    @GetMapping("/{memberId}")
    fun getMemberInfo(
        @PathVariable memberId: UUID,
        @RequestHeader("X-Internal-Secret") secret: String
    ): InternalMemberResponse {
        if (secret != internalSecret) throw ResponseStatusException(HttpStatus.FORBIDDEN)
        val member = memberService.findById(memberId) ?: throw NoSuchElementException("Member not found")
        return InternalMemberResponse(member.id!!, member.nickname)
    }
}
