package com.quietchatter.member.adaptor.`in`.web.internal

import com.quietchatter.member.dto.TokenRotationResult
import com.quietchatter.member.infrastructure.AuthTokenService
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(AuthInternalController::class)
@TestPropertySource(properties = ["INTERNAL_SECRET=test-secret"])
class AuthInternalControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockitoBean
    private lateinit var authTokenService: AuthTokenService

    @Test
    fun `refresh sets token cookies and returns memberId when refresh token is valid`() {
        val result = TokenRotationResult(
            accessToken = "new-access-token",
            refreshToken = "new-refresh-token",
            memberId = "member-uuid"
        )
        whenever(authTokenService.rotateRefreshToken("valid-refresh-token")).thenReturn(result)

        mockMvc.perform(
            post("/internal/auth/refresh")
                .header("X-Internal-Secret", "test-secret")
                .header("X-Refresh-Token", "valid-refresh-token")
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.memberId").value("member-uuid"))
            .andExpect(jsonPath("$.accessToken").doesNotExist())
            .andExpect(jsonPath("$.refreshToken").doesNotExist())

        verify(authTokenService).putRotatedTokensInCookies(any(), any())
    }

    @Test
    fun `refresh returns 401 when refresh token is expired or not in Redis`() {
        whenever(authTokenService.rotateRefreshToken("stale-token")).thenReturn(null)

        mockMvc.perform(
            post("/internal/auth/refresh")
                .header("X-Internal-Secret", "test-secret")
                .header("X-Refresh-Token", "stale-token")
        )
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun `refresh returns 403 when internal secret is wrong`() {
        mockMvc.perform(
            post("/internal/auth/refresh")
                .header("X-Internal-Secret", "wrong-secret")
                .header("X-Refresh-Token", "some-token")
        )
            .andExpect(status().isForbidden)
    }
}
