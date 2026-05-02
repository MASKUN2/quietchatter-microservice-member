package com.quietchatter.member.adaptor.`in`.web

import org.springframework.core.io.ClassPathResource
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import java.nio.charset.StandardCharsets

@RestController
class SpecController {

    @GetMapping("/api/spec")
    fun getSpec(): ResponseEntity<String> {
        return try {
            val resource = ClassPathResource("static/docs/openapi3.yaml")
            if (resource.exists()) {
                val yaml = resource.inputStream.bufferedReader(StandardCharsets.UTF_8).readText()
                ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("application/x-yaml"))
                    .body(yaml)
            } else {
                ResponseEntity.notFound().build()
            }
        } catch (e: Exception) {
            ResponseEntity.internalServerError().build()
        }
    }
}
