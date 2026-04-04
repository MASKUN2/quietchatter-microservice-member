package com.quietchatter.member.adaptor.out.external

import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import java.util.*

@Component
class TalkServiceClient(
    restClientBuilder: RestClient.Builder
) {
    private val restClient = restClientBuilder.baseUrl("http://microservice-talk").build()

    fun hideAllByMember(memberId: UUID) {
        restClient.delete()
            .uri("/internal/v1/talks/by-member/{memberId}", memberId)
            .retrieve()
            .toBodilessEntity()
    }
}
