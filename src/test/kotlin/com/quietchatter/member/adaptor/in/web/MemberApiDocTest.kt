package com.quietchatter.member.adaptor.`in`.web

import com.epages.restdocs.apispec.ResourceDocumentation.resource
import com.epages.restdocs.apispec.ResourceSnippetParameters
import com.epages.restdocs.apispec.Schema
import com.fasterxml.jackson.databind.ObjectMapper
import com.quietchatter.member.application.MemberService
import com.quietchatter.member.domain.Member
import com.quietchatter.member.domain.OauthProvider
import com.quietchatter.member.domain.Role
import com.quietchatter.member.domain.Status
import com.quietchatter.member.dto.UpdateProfileRequest
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.util.*

@WebMvcTest(AuthController::class, MeController::class)
@AutoConfigureRestDocs
@Tag("restdocs")
class MemberApiDocTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockitoBean
    private lateinit var memberService: MemberService

    @MockitoBean
    private lateinit var authTokenService: com.quietchatter.member.infrastructure.AuthTokenService

    @Test
    fun getMe() {
        val memberId = UUID.randomUUID()
        val member = Member(
            providerId = "naverId",
            provider = OauthProvider.NAVER,
            nickname = "testUser",
            role = Role.REGULAR,
            status = Status.ACTIVE
        )
        // Reflection to set ID
        val idField = member.javaClass.superclass.getDeclaredField("id")
        idField.isAccessible = true
        idField.set(member, memberId)

        whenever(memberService.findById(memberId)).thenReturn(member)

        mockMvc.perform(
            get("/api/auth/me")
                .header("X-Member-Id", memberId.toString())
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.isLoggedIn").value(true))
            .andExpect(jsonPath("$.nickname").value("testUser"))
            .andDo(
                document(
                    "auth-me",
                    resource(
                        ResourceSnippetParameters.builder()
                            .tag("Member")
                            .description("Get current authenticated user info")
                            .responseFields(
                                fieldWithPath("isLoggedIn").description("Login status"),
                                fieldWithPath("id").description("User ID").optional(),
                                fieldWithPath("role").description("User Role").optional(),
                                fieldWithPath("nickname").description("User Nickname").optional()
                            )
                            .responseSchema(Schema.schema("AuthMeResponse"))
                            .build()
                    )
                )
            )
    }

    @Test
    fun updateProfile() {
        val memberId = UUID.randomUUID()
        val request = UpdateProfileRequest(nickname = "newNick")

        mockMvc.perform(
            put("/api/members/me/profile")
                .header("X-Member-Id", memberId.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isNoContent)
            .andDo(
                document(
                    "update-profile",
                    resource(
                        ResourceSnippetParameters.builder()
                            .tag("Member")
                            .description("Update member profile nickname")
                            .requestFields(
                                fieldWithPath("nickname").description("New nickname (2-10 chars, alphanumeric/hangeul)")
                            )
                            .build()
                    )
                )
            )
    }

    @Test
    fun withdraw() {
        val memberId = UUID.randomUUID()

        mockMvc.perform(
            delete("/api/members/me")
                .header("X-Member-Id", memberId.toString())
        )
            .andExpect(status().isNoContent)
            .andDo(
                document(
                    "withdraw",
                    resource(
                        ResourceSnippetParameters.builder()
                            .tag("Member")
                            .description("Withdraw membership (Deactivate account)")
                            .build()
                    )
                )
            )
    }

    @Test
    fun loginNaver() {
        val request = mapOf("code" to "mockCode", "state" to "mockState")
        val response = com.quietchatter.member.dto.NaverLoginResponse(
            registered = true,
            registerToken = null,
            tempNickname = null
        )

        whenever(memberService.loginWithNaver(any(), any())).thenReturn(response)

        mockMvc.perform(
            post("/api/auth/login/naver")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)
            .andDo(
                document(
                    "login-naver",
                    resource(
                        ResourceSnippetParameters.builder()
                            .tag("Auth")
                            .description("Login with Naver OAuth")
                            .requestFields(
                                fieldWithPath("code").description("OAuth authorization code"),
                                fieldWithPath("state").description("OAuth state parameter")
                            )
                            .responseFields(
                                fieldWithPath("registered").description("True if user is already registered"),
                                fieldWithPath("registerToken").description("Registration token (if not registered)").optional(),
                                fieldWithPath("tempNickname").description("Temporary nickname (if not registered)").optional()
                            )
                            .build()
                    )
                )
            )
    }
}
