package com.zerost.api.auth.presentation

import com.zerost.api.auth.application.AuthLoginService
import com.zerost.api.auth.presentation.dto.LoginResponse
import com.zerost.api.common.exception.GlobalExceptionHandler
import com.zerost.api.support.createLoginRequestBody
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
    private lateinit var mockMvc: MockMvc

    @BeforeEach
    fun setUp() {
        val validator = LocalValidatorFactoryBean().apply { afterPropertiesSet() }
        mockMvc = MockMvcBuilders.standaloneSetup(AuthController(authLoginService))
            .setControllerAdvice(GlobalExceptionHandler())
            .setValidator(validator)
            .build()
    }

    @Test
    fun `로그인 요청이 올바르면 로그인 결과를 반환한다`() {
        `when`(authLoginService.login("010-1234-5678", "zerost1234")).thenReturn(
            LoginResponse(
                userId = 1L,
                deviceId = "device-1",
                nickname = "펭귄탐험가",
                phoneNumber = "010-1234-5678",
                onboardingCompleted = true,
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
            .andExpect(jsonPath("$.data.deviceId").value("device-1"))

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
}
