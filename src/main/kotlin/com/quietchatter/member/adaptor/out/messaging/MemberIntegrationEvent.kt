package com.quietchatter.member.adaptor.out.messaging

import com.fasterxml.jackson.annotation.JsonAnyGetter
import com.fasterxml.jackson.annotation.JsonProperty

data class MemberIntegrationEvent(
    @JsonProperty("evt_id") val evtId: String,
    @JsonProperty("evt_agg_id") val evtAggId: String,
    @JsonProperty("evt_type") val evtType: String,
    @JsonProperty("evt_time") val evtTime: String,
    @JsonAnyGetter val payload: Map<String, Any?>
)