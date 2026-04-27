package com.quietchatter.customer.domain

import com.quietchatter.member.persistence.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table

@Entity
@Table(name = "customer_message")
class CustomerMessage(
    @Column(name = "message", columnDefinition = "TEXT")
    var message: String
) : BaseEntity()
