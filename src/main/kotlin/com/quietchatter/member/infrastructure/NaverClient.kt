package com.quietchatter.member.infrastructure

import com.quietchatter.member.dto.NaverProfileResponse
import com.quietchatter.member.dto.NaverTokenResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import org.springframework.web.util.UriComponentsBuilder

@Component
class NaverClient(
    restClientBuilder: RestClient.Builder,
    @Value("\${naver.api.client-id}") private val clientId: String,
    @Value("\${naver.api.client-secret}") private val clientSecret: String
) {
    private val restClient: RestClient = restClientBuilder.build()

    fun getAccessToken(code: String, state: String): NaverTokenResponse? {
        val uri = UriComponentsBuilder.fromHttpUrl("https://nid.naver.com/oauth2.0/token")
            .queryParam("grant_type", "authorization_code")
            .queryParam("client_id", clientId)
            .queryParam("client_secret", clientSecret)
            .queryParam("code", code)
            .queryParam("state", state)
            .toUriString()

        return restClient.get()
            .uri(uri)
            .retrieve()
            .body(NaverTokenResponse::class.java)
    }

    fun getProfile(accessToken: String): NaverProfileResponse? {
        return restClient.get()
            .uri("https://openapi.naver.com/v1/nid/me")
            .header("Authorization", "Bearer $accessToken")
            .retrieve()
            .body(NaverProfileResponse::class.java)
    }
}
