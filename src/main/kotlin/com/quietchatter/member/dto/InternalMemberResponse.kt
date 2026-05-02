package com.quietchatter.member.dto
import java.util.UUID

data class InternalMemberResponse(
    val id: UUID,
    val nickname: String
)
