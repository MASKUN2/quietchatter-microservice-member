package com.quietchatter.customer.application.service

import com.quietchatter.customer.application.in.CustomerMessageCreatable
import com.quietchatter.customer.application.out.CustomerMessageRepository
import com.quietchatter.customer.domain.CustomerMessage
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class CustomerMessageService(
    private val customerMessageRepository: CustomerMessageRepository
) : CustomerMessageCreatable {
    override fun create(command: CustomerMessageCreatable.CreateCommand) {
        customerMessageRepository.save(CustomerMessage(command.content))
    }
}
