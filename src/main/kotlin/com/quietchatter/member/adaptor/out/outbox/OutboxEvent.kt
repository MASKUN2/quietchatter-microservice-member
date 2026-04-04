package com.quietchatter.member.adaptor.out.outbox

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "outbox_event")
class OutboxEvent(
    @Id
    @Column(name = "id")
    val id: UUID = UUID.randomUUID(),

    @Column(name = "aggregate_type")
    val aggregateType: String,

    @Column(name = "aggregate_id")
    val aggregateId: String,

    @Column(name = "type")
    val type: String,

    @Column(name = "payload")
    val payload: String,

    @Column(name = "created_at")
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "processed_at")
    var processedAt: LocalDateTime? = null
) {
    fun markProcessed() {
        this.processedAt = LocalDateTime.now()
    }
}