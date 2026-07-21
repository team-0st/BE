package com.zerost.api.communitymission.presentation

import com.zerost.api.common.device.DeviceIdInterceptor
import com.zerost.api.common.exception.GlobalExceptionHandler
import com.zerost.api.communitymission.application.CommunityMissionCompletionService
import com.zerost.api.communitymission.application.CommunityMissionProofService
import com.zerost.api.communitymission.application.CommunityMissionQueryService
import com.zerost.api.communitymission.presentation.dto.CompleteCommunityMissionResponse
import com.zerost.api.communitymission.presentation.dto.CommunityMissionDetailResponse
import com.zerost.api.communitymission.presentation.dto.CommunityMissionProofRequirementResponse
import com.zerost.api.communitymission.presentation.dto.CommunityMissionRewardedIngredientResponse
import com.zerost.api.communitymission.presentation.dto.CommunityMissionProgressResponse
import com.zerost.api.support.createAuthTokenProvider
import com.zerost.api.support.createSubmitCommunityMissionProofRequestBody
import com.zerost.api.communitymission.presentation.dto.SubmitCommunityMissionProofResponse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import java.math.BigDecimal

class CommunityMissionControllerTest {

    private val communityMissionQueryService = mock(CommunityMissionQueryService::class.java)
    private val communityMissionProofService = mock(CommunityMissionProofService::class.java)
    private val communityMissionCompletionService = mock(CommunityMissionCompletionService::class.java)
    private lateinit var mockMvc: MockMvc

    @BeforeEach
    fun setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(
            CommunityMissionController(communityMissionQueryService, communityMissionProofService, communityMissionCompletionService),
        )
            .setControllerAdvice(GlobalExceptionHandler())
            .addInterceptors(DeviceIdInterceptor(createAuthTokenProvider()))
            .build()
    }

    @Test
    fun `인증 토큰이 있으면 공동 미션 진행률을 조회할 수 있다`() {
        `when`(communityMissionQueryService.getCommunityMissions(1L)).thenReturn(
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
                    requiredProofCount = 1,
                    submittedProofCount = 1,
                    approvedProofCount = 1,
                    readyToComplete = false,
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

        verify(communityMissionQueryService).getCommunityMissions(1L)
    }

    @Test
    fun `인증 토큰이 있으면 공동 미션 상세를 조회할 수 있다`() {
        `when`(communityMissionQueryService.getCommunityMission(1L, 3L)).thenReturn(
            CommunityMissionDetailResponse(
                id = 3L,
                title = "오늘의 친환경 약속",
                description = "설명",
                imageUrl = "image-1",
                difficulty = "ONE_STAR",
                stage = 1,
                targetRatio = BigDecimal("30.00"),
                succeeded = false,
                unlocked = true,
                completed = false,
                requiredProofCount = 1,
                submittedProofCount = 0,
                approvedProofCount = 0,
                readyToComplete = false,
                proofRequirements = listOf(
                    CommunityMissionProofRequirementResponse(
                        requirementId = 11L,
                        proofOrder = 1,
                        title = "1일차 인증",
                        description = "사진 제출",
                        requiredImageCount = 1,
                        requiredDayOffset = null,
                        submitted = false,
                        submittedProofId = null,
                        submittedAt = null,
                        reviewStatus = null,
                        reviewedAt = null,
                        submittedImageKeys = emptyList(),
                    ),
                ),
            ),
        )

        mockMvc.perform(
            get("/api/v1/community-missions/3")
                .header("Authorization", "Bearer access-token"),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data.id").value(3))
            .andExpect(jsonPath("$.data.proofRequirements[0].requirementId").value(11))
            .andExpect(jsonPath("$.data.requiredProofCount").value(1))

        verify(communityMissionQueryService).getCommunityMission(1L, 3L)
    }

    @Test
    fun `인증 토큰이 있으면 공동 미션 인증 단계를 제출할 수 있다`() {
        `when`(communityMissionProofService.submitProof(1L, 3L, 11L, listOf("community-missions/1/1/2026/07/21/proof-1.jpg"))).thenReturn(
            SubmitCommunityMissionProofResponse(
                proofId = 101L,
                communityMissionId = 3L,
                requirementId = 11L,
                proofOrder = 1,
                submittedAt = "2026-07-21T15:30:00",
                readyToComplete = true,
            ),
        )

        mockMvc.perform(
            post("/api/v1/community-missions/3/proofs/11")
                .header("Authorization", "Bearer access-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createSubmitCommunityMissionProofRequestBody()),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data.proofId").value(101))
            .andExpect(jsonPath("$.data.requirementId").value(11))
            .andExpect(jsonPath("$.data.readyToComplete").value(true))

        verify(communityMissionProofService).submitProof(1L, 3L, 11L, listOf("community-missions/1/1/2026/07/21/proof-1.jpg"))
    }

    @Test
    fun `인증 토큰이 있으면 공동 미션 완료 처리와 보상 결과를 조회할 수 있다`() {
        `when`(communityMissionCompletionService.complete(1L, 3L)).thenReturn(
            CompleteCommunityMissionResponse(
                completionId = 11L,
                communityMissionId = 3L,
                succeeded = true,
                rewardGranted = true,
                rewardedEcoJam = 50,
                rewardedIngredients = listOf(
                    CommunityMissionRewardedIngredientResponse(
                        ingredientId = 1L,
                        ingredientName = "토마토",
                        ingredientType = "COMMON",
                        quantity = 1,
                    ),
                ),
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
            .andExpect(jsonPath("$.data.succeeded").value(true))
            .andExpect(jsonPath("$.data.rewardGranted").value(true))
            .andExpect(jsonPath("$.data.rewardedEcoJam").value(50))
            .andExpect(jsonPath("$.data.rewardedIngredients[0].ingredientName").value("토마토"))
            .andExpect(jsonPath("$.data.completedAt").value("2026-07-21T15:30:00"))

        verify(communityMissionCompletionService).complete(1L, 3L)
    }
}
