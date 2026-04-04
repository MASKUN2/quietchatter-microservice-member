package com.quietchatter.member.adaptor.out.outbox

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
    private val streamBridge: StreamBridge
) {
    private val log = LoggerFactory.getLogger(OutboxRelayService::class.java)

    @Scheduled(fixedDelayString = "\${outbox.relay.fixed-delay:1000}")
    @Transactional
    fun relayEvents() {
        val events = outboxEventRepository.findByProcessedAtIsNullOrderByCreatedAtAsc(PageRequest.of(0, 100))
        for (event in events) {
            val message = MessageBuilder.withPayload(event.payload)
                .setHeader(KafkaHeaders.KEY, event.aggregateId.toByteArray())
                .setHeader("eventType", event.type)
                .build()

            val success = streamBridge.send("memberEvents-out-0", message)
            if (success) {
                event.markProcessed()
                log.debug("Successfully relayed outbox event: \${event.id}")
            } else {
                log.error("Failed to relay outbox event: \${event.id}")
            }
        }
    }
}