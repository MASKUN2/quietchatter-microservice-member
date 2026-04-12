package com.quietchatter.member.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
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
    @field:NotBlank
    @field:Size(min = 2, max = 10)
    @field:Pattern(regexp = "^[가-힣a-zA-Z0-9]+$")
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
    @field:NotBlank
    @field:Size(min = 2, max = 10)
    @field:Pattern(regexp = "^[가-힣a-zA-Z0-9]+$")
    val nickname: String
)
