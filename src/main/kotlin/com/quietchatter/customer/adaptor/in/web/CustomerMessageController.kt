package com.quietchatter.customer.adaptor.in.web

import com.quietchatter.customer.application.in.CustomerMessageCreatable
import jakarta.validation.Valid
import jakarta.validation.constraints.Size
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/v1/customer")
class CustomerMessageController(
    private val customerMessageCreatable: CustomerMessageCreatable
) {
    @PostMapping("/messages")
    @ResponseStatus(HttpStatus.ACCEPTED)
    fun create(@RequestBody @Valid request: CreateRequest) {
        customerMessageCreatable.create(CustomerMessageCreatable.CreateCommand(request.content))
    }

    data class CreateRequest(
        @field:Size(min = 1, max = 500)
        val content: String
    )
}
