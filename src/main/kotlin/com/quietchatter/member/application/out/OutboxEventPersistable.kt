package com.quietchatter.member.application.out

import com.quietchatter.member.adaptor.out.outbox.OutboxEvent
import java.time.LocalDateTime

interface OutboxEventPersistable {
    fun save(event: OutboxEvent): OutboxEvent
    fun findUnprocessed(limit: Int): List<OutboxEvent>
    fun deleteProcessedBefore(cutoff: LocalDateTime): Long
}
