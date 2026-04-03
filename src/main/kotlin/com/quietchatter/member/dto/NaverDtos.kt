package com.quietchatter.member.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class NaverTokenResponse(
    @JsonProperty("access_token") val accessToken: String?,
    @JsonProperty("refresh_token") val refreshToken: String?,
    @JsonProperty("token_type") val tokenType: String?,
    @JsonProperty("expires_in") val expiresIn: String?,
    @JsonProperty("error") val error: String?,
    @JsonProperty("error_description") val errorDescription: String?
)

data class NaverProfileResponse(
    @JsonProperty("resultcode") val resultCode: String,
    @JsonProperty("message") val message: String,
    @JsonProperty("response") val response: NaverProfileData?
)

data class NaverProfileData(
    val id: String,
    val nickname: String?,
    val email: String?,
    @JsonProperty("profile_image") val profileImage: String?
)
