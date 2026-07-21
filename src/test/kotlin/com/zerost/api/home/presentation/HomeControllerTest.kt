package com.zerost.api.home.presentation

import com.zerost.api.common.device.DeviceIdInterceptor
import com.zerost.api.common.exception.GlobalExceptionHandler
import com.zerost.api.home.application.HomeQueryService
import com.zerost.api.home.presentation.dto.HomeMissionProgressResponse
import com.zerost.api.home.presentation.dto.HomeResponse
import com.zerost.api.support.createAuthTokenProvider
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders

class HomeControllerTest {

    private val homeQueryService = mock(HomeQueryService::class.java)
    private lateinit var mockMvc: MockMvc

    @BeforeEach
    fun setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(HomeController(homeQueryService))
            .setControllerAdvice(GlobalExceptionHandler())
            .addInterceptors(DeviceIdInterceptor(createAuthTokenProvider()))
            .build()
    }

    @Test
    fun `인증된 사용자는 홈 화면 통합 조회를 할 수 있다`() {
        `when`(homeQueryService.getHome(1L)).thenReturn(
            HomeResponse(
                nickname = "펭귄탐험가",
                ecoJam = 320,
                point = 1500,
                checkedInToday = true,
                missionProgress = HomeMissionProgressResponse(
                    totalMissionCount = 7,
                    submittedMissionCount = 2,
                    pendingMissionCount = 1,
                    approvedMissionCount = 1,
                    rejectedMissionCount = 0,
                ),
            ),
        )

        mockMvc.perform(
            get("/api/v1/home")
                .header("Authorization", "Bearer access-token"),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.nickname").value("펭귄탐험가"))
            .andExpect(jsonPath("$.data.checkedInToday").value(true))
            .andExpect(jsonPath("$.data.missionProgress.totalMissionCount").value(7))

        verify(homeQueryService).getHome(1L)
    }
}
