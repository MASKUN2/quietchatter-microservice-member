package com.quietchatter.member.adaptor.out.messaging

import com.fasterxml.jackson.annotation.JsonProperty

data class MemberIntegrationEvent(
    @JsonProperty("specversion") val specVersion: String = "1.0",
    @JsonProperty("id") val id: String,
    @JsonProperty("source") val source: String,
    @JsonProperty("type") val type: String,
    @JsonProperty("time") val time: String,
    @JsonProperty("subject") val subject: String,
    @JsonProperty("datacontenttype") val dataContentType: String = "application/json",
    @JsonProperty("data") val data: Map<String, Any?>
)
