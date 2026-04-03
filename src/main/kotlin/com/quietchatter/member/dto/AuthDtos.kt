package com.quietchatter.member.dto

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
    val registerToken: String,
    val nickname: String
)
