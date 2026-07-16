package com.zerost.api.user.presentation

import com.zerost.api.common.exception.GlobalExceptionHandler
import com.zerost.api.support.createRegisterUserRequestBody
import com.zerost.api.user.application.UserRegistrationService
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

class UserControllerTest {

    private val userRegistrationService = mock(UserRegistrationService::class.java)
    private lateinit var mockMvc: MockMvc

    @BeforeEach
    fun setUp() {
        val validator = LocalValidatorFactoryBean().apply { afterPropertiesSet() }
        mockMvc = MockMvcBuilders.standaloneSetup(UserController(userRegistrationService))
            .setControllerAdvice(GlobalExceptionHandler())
            .setValidator(validator)
            .build()
    }

    @Test
    fun `디바이스 등록에 성공하면 유저 정보를 반환한다`() {
        `when`(userRegistrationService.register("device-1"))
            .thenReturn(RegisterUserResponse(userId = 1L, onboardingCompleted = false))

        mockMvc.perform(
                post("/api/v1/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createRegisterUserRequestBody()),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.userId").value(1))
            .andExpect(jsonPath("$.data.onboardingCompleted").value(false))

        verify(userRegistrationService).register("device-1")
    }

    @Test
    fun `디바이스 아이디가 비어 있으면 등록에 실패한다`() {
        mockMvc.perform(
                post("/api/v1/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createRegisterUserRequestBody(deviceId = "")),
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("INVALID_INPUT_VALUE"))
    }
}
