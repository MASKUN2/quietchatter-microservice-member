package com.quietchatter.customer.adaptor.out.persistence
import com.quietchatter.customer.application.out.CustomerMessageRepository
import com.quietchatter.customer.domain.CustomerMessage
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface CustomerMessageJpaRepository : JpaRepository<CustomerMessage, UUID>, CustomerMessageRepository
