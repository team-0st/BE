package com.zerost.api.auth.presentation

import com.zerost.api.auth.application.AuthLoginService
import com.zerost.api.auth.application.AuthTokenService
import com.zerost.api.auth.presentation.dto.LoginResponse
import com.zerost.api.auth.presentation.dto.RefreshTokenResponse
import com.zerost.api.common.exception.GlobalExceptionHandler
import com.zerost.api.support.createLoginRequestBody
import com.zerost.api.support.createRefreshTokenRequestBody
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean

class AuthControllerTest {

    private val authLoginService = mock(AuthLoginService::class.java)
    private val authTokenService = mock(AuthTokenService::class.java)
    private lateinit var mockMvc: MockMvc

    @BeforeEach
    fun setUp() {
        val validator = LocalValidatorFactoryBean().apply { afterPropertiesSet() }
        mockMvc = MockMvcBuilders.standaloneSetup(AuthController(authLoginService, authTokenService))
            .setControllerAdvice(GlobalExceptionHandler())
            .setValidator(validator)
            .build()
    }

    @Test
    fun `로그인 요청이 올바르면 로그인 결과를 반환한다`() {
        `when`(authLoginService.login("010-1234-5678", "zerost1234")).thenReturn(
            LoginResponse(
                userId = 1L,
                nickname = "펭귄탐험가",
                phoneNumber = "010-1234-5678",
                onboardingCompleted = true,
                profileCharacterCode = "TOMATO",
                profileCharacterImageUrl = "https://assets.zero-st.com/profile-characters/tomato.png",
                accessToken = "access-token",
                refreshToken = "refresh-token",
                tokenType = "Bearer",
                accessTokenExpiresIn = 3600,
                refreshTokenExpiresIn = 1209600,
            ),
        )

        mockMvc.perform(
            post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createLoginRequestBody()),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.userId").value(1))
            .andExpect(jsonPath("$.data.profileCharacterCode").value("TOMATO"))
            .andExpect(jsonPath("$.data.profileCharacterImageUrl").value("https://assets.zero-st.com/profile-characters/tomato.png"))
            .andExpect(jsonPath("$.data.accessToken").value("access-token"))
            .andExpect(jsonPath("$.data.refreshToken").value("refresh-token"))

        verify(authLoginService).login("010-1234-5678", "zerost1234")
    }

    @Test
    fun `휴대전화 번호 형식이 올바르지 않으면 로그인에 실패한다`() {
        mockMvc.perform(
            post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createLoginRequestBody(phoneNumber = "01012345678")),
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("INVALID_INPUT_VALUE"))
    }

    @Test
    fun `010이 아닌 휴대전화 번호 prefix면 로그인에 실패한다`() {
        mockMvc.perform(
            post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createLoginRequestBody(phoneNumber = "011-1234-5678")),
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("INVALID_INPUT_VALUE"))
    }

    @Test
    fun `유효한 refresh token이면 토큰을 재발급한다`() {
        `when`(authTokenService.refresh("refresh-token")).thenReturn(
            RefreshTokenResponse(
                accessToken = "new-access-token",
                refreshToken = "new-refresh-token",
                tokenType = "Bearer",
                accessTokenExpiresIn = 3600,
                refreshTokenExpiresIn = 1209600,
            ),
        )

        mockMvc.perform(
            post("/api/v1/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createRefreshTokenRequestBody()),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data.accessToken").value("new-access-token"))
            .andExpect(jsonPath("$.data.refreshToken").value("new-refresh-token"))

        verify(authTokenService).refresh("refresh-token")
    }

    @Test
    fun `유효한 refresh token이면 로그아웃할 수 있다`() {
        mockMvc.perform(
            post("/api/v1/auth/logout")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createRefreshTokenRequestBody()),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))

        verify(authTokenService).logout("refresh-token")
    }
}
