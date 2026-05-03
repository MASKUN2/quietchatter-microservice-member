package com.quietchatter.member.adaptor.out.outbox

import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDateTime
import java.util.UUID

interface OutboxEventRepository : JpaRepository<OutboxEvent, UUID> {
    fun findByProcessedAtIsNullOrderByCreatedAtAsc(pageable: Pageable): List<OutboxEvent>
    fun deleteByProcessedAtIsNotNullAndProcessedAtBefore(cutoff: LocalDateTime): Long
}