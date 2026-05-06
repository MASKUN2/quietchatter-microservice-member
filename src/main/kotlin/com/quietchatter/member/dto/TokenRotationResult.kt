package com.quietchatter.member.dto

data class TokenRotationResult(
    val accessToken: String,
    val refreshToken: String,
    val memberId: String
)
