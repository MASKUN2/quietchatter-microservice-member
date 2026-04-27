package com.quietchatter.customer.application.out
import com.quietchatter.customer.domain.CustomerMessage
interface CustomerMessageRepository {
    fun save(customerMessage: CustomerMessage): CustomerMessage
}
