package com.quietchatter.member.adaptor.`in`.web.internal

import com.quietchatter.member.application.MemberService
import com.quietchatter.member.dto.InternalMemberResponse
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/internal/api/members")
class MemberInternalController(
    private val memberService: MemberService
) {
    @GetMapping("/{memberId}")
    fun getMemberInfo(@PathVariable memberId: UUID): InternalMemberResponse {
        val member = memberService.findById(memberId) ?: throw NoSuchElementException("Member not found")
        return InternalMemberResponse(member.id!!, member.nickname)
    }
}
