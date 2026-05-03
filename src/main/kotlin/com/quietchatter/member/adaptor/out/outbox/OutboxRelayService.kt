package com.quietchatter.member.adaptor.out.outbox

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.quietchatter.member.adaptor.out.messaging.MemberIntegrationEvent
import org.slf4j.LoggerFactory
import org.springframework.cloud.stream.function.StreamBridge
import org.springframework.data.domain.PageRequest
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.messaging.support.MessageBuilder
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class OutboxRelayService(
    private val outboxEventRepository: OutboxEventRepository,
    private val streamBridge: StreamBridge,
    private val objectMapper: ObjectMapper
) {
    private val log = LoggerFactory.getLogger(OutboxRelayService::class.java)

    @Scheduled(fixedDelayString = "\${outbox.relay.fixed-delay:1000}")
    @Transactional
    fun relayEvents() {
        val events = outboxEventRepository.findByProcessedAtIsNullOrderByCreatedAtAsc(PageRequest.of(0, 100))
        events.forEach { event ->
            runCatching {
                val payloadMap: Map<String, Any?> = objectMapper.readValue(event.payload)
                val integrationEvent = MemberIntegrationEvent(
                    evtId = event.id.toString(),
                    evtAggId = event.aggregateId,
                    evtType = event.type,
                    evtTime = event.createdAt.toString(),
                    payload = payloadMap
                )

                val message = MessageBuilder.withPayload(integrationEvent)
                    .setHeader(KafkaHeaders.KEY, event.aggregateId.toByteArray())
                    .build()

                if (streamBridge.send("memberEvents-out-0", message)) {
                    event.markProcessed()
                    outboxEventRepository.save(event)
                    log.debug("Successfully relayed outbox event: ${event.id}")
                } else {
                    log.error("Failed to relay outbox event: ${event.id}")
                }
            }.onFailure { e ->
                log.error("Error processing outbox event: ${event.id}", e)
            }
        }
    }
}
