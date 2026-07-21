package com.zerost.api.checkin.presentation

import com.zerost.api.checkin.application.CheckInService
import com.zerost.api.checkin.presentation.dto.CheckInResponse
import com.zerost.api.checkin.presentation.dto.CheckInStatusResponse
import com.zerost.api.checkin.presentation.dto.RewardedIngredientResponse
import com.zerost.api.common.auth.AuthenticationInterceptor
import com.zerost.api.common.exception.GlobalExceptionHandler
import com.zerost.api.support.createAuthTokenProvider
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders

class CheckInControllerTest {

    private val checkInService = mock(CheckInService::class.java)
    private lateinit var mockMvc: MockMvc

    @BeforeEach
    fun setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(CheckInController(checkInService))
            .setControllerAdvice(GlobalExceptionHandler())
            .addInterceptors(AuthenticationInterceptor(createAuthTokenProvider()))
            .build()
    }

    @Test
    fun `인증된 사용자는 출석 처리 결과를 반환한다`() {
        val response = CheckInResponse(
            rewardedIngredient = RewardedIngredientResponse(
                id = 1L,
                name = "버려진 천",
                type = "COMMON",
                imageUrl = "image-1",
            ),
        )
        `when`(checkInService.checkIn(1L)).thenReturn(response)

        mockMvc.perform(
            post("/api/v1/check-in")
                .header("Authorization", "Bearer access-token"),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.rewardedIngredient.id").value(1))
            .andExpect(jsonPath("$.data.rewardedIngredient.name").value("버려진 천"))

        verify(checkInService).checkIn(1L)
    }

    @Test
    fun `인증된 사용자는 오늘 출석 여부를 조회할 수 있다`() {
        `when`(checkInService.getTodayStatus(1L)).thenReturn(CheckInStatusResponse(checkedIn = true))

        mockMvc.perform(
            get("/api/v1/check-in/status")
                .header("Authorization", "Bearer access-token"),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.checkedIn").value(true))

        verify(checkInService).getTodayStatus(1L)
    }
}
