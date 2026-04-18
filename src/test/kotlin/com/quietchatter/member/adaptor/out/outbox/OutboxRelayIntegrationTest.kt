package com.quietchatter.member.adaptor.out.outbox

import com.quietchatter.member.application.MemberService
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.PageRequest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.KafkaContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import java.util.UUID

@Disabled("Docker is not available in current CI environment")
@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
class OutboxRelayIntegrationTest {

    @Autowired
    private lateinit var outboxEventRepository: OutboxEventRepository

    @Autowired
    private lateinit var outboxRelayService: OutboxRelayService

    companion object {
        @Container
        val kafka = KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.4.0"))

        @JvmStatic
        @DynamicPropertySource
        fun registerKafkaProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.cloud.stream.kafka.binder.brokers", kafka::getBootstrapServers)
            registry.add("spring.cloud.stream.kafka.binder.configuration.schema.registry.url") { "http://localhost:8081" }
            // Disable actual scheduled running during context load if we want manual testing
            registry.add("outbox.relay.fixed-delay") { "9999999" }
        }
    }

    @Test
    fun `should relay unsent events and mark them as processed`() {
        // Given
        val event = OutboxEvent(
            aggregateType = "Member",
            aggregateId = UUID.randomUUID().toString(),
            type = "MemberRegisteredEvent",
            payload = "{}"
        )
        outboxEventRepository.save(event)

        // When
        outboxRelayService.relayEvents()

        // Then
        val processedEvent = outboxEventRepository.findById(event.id).orElseThrow()
        assertNotNull(processedEvent.processedAt, "ProcessedAt should not be null after relaying")
    }
}