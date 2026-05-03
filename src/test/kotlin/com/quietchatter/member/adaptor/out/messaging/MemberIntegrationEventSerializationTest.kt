package com.quietchatter.member.adaptor.out.messaging

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class MemberIntegrationEventSerializationTest {

    @Test
    fun `should serialize MemberIntegrationEvent in CloudEvents format`() {
        val objectMapper = ObjectMapper().registerKotlinModule()
        val event = MemberIntegrationEvent(
            id = "event-id",
            source = "/member",
            type = "com.quietchatter.member.MemberDeactivatedEvent",
            time = "2026-05-03T18:00:00+00:00",
            subject = "agg-id",
            data = mapOf("memberId" to "123e4567-e89b-12d3-a456-426614174000")
        )

        val json = objectMapper.writeValueAsString(event)

        assertTrue(json.contains("\"specversion\":\"1.0\""))
        assertTrue(json.contains("\"source\":\"/member\""))
        assertTrue(json.contains("\"type\":\"com.quietchatter.member.MemberDeactivatedEvent\""))
        assertTrue(json.contains("\"subject\":\"agg-id\""))
        assertTrue(json.contains("\"data\":{\"memberId\":\"123e4567-e89b-12d3-a456-426614174000\"}"))
    }
}
