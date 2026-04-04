package com.quietchatter.member.dto

import java.util.*

data class NaverLoginRequest(
    val code: String,
    val state: String
)

data class NaverLoginResponse(
    val registered: Boolean,
    val registerToken: String? = null,
    val tempNickname: String? = null
) {
    companion object {
        fun registered() = NaverLoginResponse(true)
        fun notRegistered(registerToken: String, tempNickname: String) = 
            NaverLoginResponse(false, registerToken, tempNickname)
    }
}

data class SignupRequest(
    val nickname: String
)

data class ReactivateRequest(
    val token: String
)

data class AuthMeResponse(
    val isLoggedIn: Boolean,
    val id: UUID? = null,
    val nickname: String? = null,
    val role: String? = null
)

data class UpdateProfileRequest(
    val nickname: String
)
