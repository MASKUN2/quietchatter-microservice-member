package com.quietchatter.member.infrastructure

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app.cookie")
data class AppCookieProperties(
    val domain: String? = null,
    val secure: Boolean = true,
    val sameSite: String = "Lax"
)
