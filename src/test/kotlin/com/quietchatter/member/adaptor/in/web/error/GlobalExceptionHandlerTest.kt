package com.quietchatter.member.adaptor.`in`.web.error

import com.quietchatter.member.adaptor.`in`.web.MeController
import com.quietchatter.member.application.MemberService
import com.quietchatter.member.infrastructure.AuthTokenService
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(MeController::class)
class GlobalExceptionHandlerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockitoBean
    private lateinit var memberService: MemberService

    @MockitoBean
    private lateinit var authTokenService: AuthTokenService

    @Test
    fun `missing X-Member-Id header on auth-required endpoint returns 401`() {
        mockMvc.perform(
            put("/api/members/me/profile")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"nickname":"tester"}""")
        ).andExpect(status().isUnauthorized)
    }
}
