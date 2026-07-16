package com.zerost.api.user.presentation

import com.zerost.api.common.device.DeviceIdInterceptor
import com.zerost.api.common.exception.GlobalExceptionHandler
import com.zerost.api.support.createOnboardingCommand
import com.zerost.api.support.createOnboardingRequestBody
import com.zerost.api.user.application.CompleteOnboardingCommand
import com.zerost.api.user.application.OnboardingService
import com.zerost.api.user.presentation.dto.CompleteOnboardingResponse
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

class OnboardingControllerTest {

    private val onboardingService = mock(OnboardingService::class.java)
    private lateinit var mockMvc: MockMvc

    @BeforeEach
    fun setUp() {
        val validator = LocalValidatorFactoryBean().apply { afterPropertiesSet() }
        mockMvc = MockMvcBuilders.standaloneSetup(OnboardingController(onboardingService))
            .setControllerAdvice(GlobalExceptionHandler())
            .setValidator(validator)
            .addInterceptors(DeviceIdInterceptor())
            .build()
    }

    @Test
    fun `온보딩 완료 요청이 올바르면 저장 결과를 반환한다`() {
        val command = createOnboardingCommand()
        val response = CompleteOnboardingResponse(
            userId = 1L,
            nickname = "펭귄탐험가",
            phoneNumber = "010-1234-5678",
            shopId = 1L,
        )
        `when`(onboardingService.complete(command)).thenReturn(response)

        mockMvc.perform(
                post("/api/v1/onboarding/complete")
                .header("X-Device-Id", "device-1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createOnboardingRequestBody()),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.userId").value(1))
            .andExpect(jsonPath("$.data.nickname").value("펭귄탐험가"))

        verify(onboardingService).complete(command)
    }

    @Test
    fun `전화번호 형식이 올바르지 않으면 온보딩 완료에 실패한다`() {
        mockMvc.perform(
            post("/api/v1/onboarding/complete")
                .header("X-Device-Id", "device-1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createOnboardingRequestBody(phoneNumber = "01012345678")),
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("INVALID_INPUT_VALUE"))
    }

    @Test
    fun `상점 아이디가 없으면 온보딩 완료에 실패한다`() {
        mockMvc.perform(
            post("/api/v1/onboarding/complete")
                .header("X-Device-Id", "device-1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createOnboardingRequestBody(shopId = null)),
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("INVALID_INPUT_VALUE"))
            .andExpect(jsonPath("$.error.message").value("상점 ID는 필수입니다."))
    }
}
