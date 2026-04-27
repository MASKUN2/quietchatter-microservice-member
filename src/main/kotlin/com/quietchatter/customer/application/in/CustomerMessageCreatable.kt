package com.quietchatter.customer.application.in

interface CustomerMessageCreatable {
    fun create(command: CreateCommand)
    data class CreateCommand(val content: String)
}
