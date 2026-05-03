package com.quietchatter.member.adaptor.out.messaging

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class MemberIntegrationEventSerializationTest {

    @Test
    fun `should serialize MemberIntegrationEvent with flattened payload`() {
        val objectMapper = ObjectMapper().registerKotlinModule()
        val payload = mapOf("memberId" to "123e4567-e89b-12d3-a456-426614174000")
        val event = MemberIntegrationEvent(
            evtId = "event-id",
            evtAggId = "agg-id",
            evtType = "MemberDeactivatedEvent",
            evtTime = "2026-05-03T18:00:00",
            payload = payload
        )

        val json = objectMapper.writeValueAsString(event)
        println("Generated JSON: $json")

        assert(json.contains("\"evt_type\":\"MemberDeactivatedEvent\""))
        assert(json.contains("\"memberId\":\"123e4567-e89b-12d3-a456-426614174000\""))
    }
}
