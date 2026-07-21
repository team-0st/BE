package com.zerost.api.communitymission.presentation

import com.zerost.api.common.device.DeviceIdInterceptor
import com.zerost.api.common.exception.GlobalExceptionHandler
import com.zerost.api.communitymission.application.CommunityMissionCompletionService
import com.zerost.api.communitymission.application.CommunityMissionQueryService
import com.zerost.api.communitymission.presentation.dto.CompleteCommunityMissionResponse
import com.zerost.api.communitymission.presentation.dto.CommunityMissionProgressResponse
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
import java.math.BigDecimal

class CommunityMissionControllerTest {

    private val communityMissionQueryService = mock(CommunityMissionQueryService::class.java)
    private val communityMissionCompletionService = mock(CommunityMissionCompletionService::class.java)
    private lateinit var mockMvc: MockMvc

    @BeforeEach
    fun setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(
            CommunityMissionController(communityMissionQueryService, communityMissionCompletionService),
        )
            .setControllerAdvice(GlobalExceptionHandler())
            .addInterceptors(DeviceIdInterceptor(createAuthTokenProvider()))
            .build()
    }

    @Test
    fun `디바이스 아이디가 있으면 공동 미션 진행률을 조회할 수 있다`() {
        `when`(communityMissionQueryService.getCommunityMissions("device-1")).thenReturn(
            listOf(
                CommunityMissionProgressResponse(
                    id = 1L,
                    title = "오늘의 친환경 약속",
                    description = "설명",
                    imageUrl = "image-1",
                    difficulty = "ONE_STAR",
                    stage = 1,
                    targetRatio = BigDecimal("30.00"),
                    achievementRatio = BigDecimal("45.50"),
                    participantCount = 91L,
                    totalUserCount = 200L,
                    succeeded = true,
                    unlocked = true,
                    completed = true,
                ),
            ),
        )

        mockMvc.perform(
            get("/api/v1/community-missions")
                .header("Authorization", "Bearer access-token"),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data[0].id").value(1))
            .andExpect(jsonPath("$.data[0].achievementRatio").value(45.50))
            .andExpect(jsonPath("$.data[0].succeeded").value(true))
            .andExpect(jsonPath("$.data[0].unlocked").value(true))
            .andExpect(jsonPath("$.data[0].completed").value(true))

        verify(communityMissionQueryService).getCommunityMissions("device-1")
    }

    @Test
    fun `디바이스 아이디가 있으면 공동 미션 완료 처리를 할 수 있다`() {
        `when`(communityMissionCompletionService.complete("device-1", 3L)).thenReturn(
            CompleteCommunityMissionResponse(
                completionId = 11L,
                communityMissionId = 3L,
                completedAt = "2026-07-21T15:30:00",
            ),
        )

        mockMvc.perform(
            post("/api/v1/community-missions/3/complete")
                .header("Authorization", "Bearer access-token"),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.completionId").value(11))
            .andExpect(jsonPath("$.data.communityMissionId").value(3))
            .andExpect(jsonPath("$.data.completedAt").value("2026-07-21T15:30:00"))

        verify(communityMissionCompletionService).complete("device-1", 3L)
    }
}
