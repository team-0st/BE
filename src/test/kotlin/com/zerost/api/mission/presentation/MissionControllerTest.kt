package com.zerost.api.mission.presentation

import com.zerost.api.common.auth.AuthenticationInterceptor
import com.zerost.api.common.exception.GlobalExceptionHandler
import com.zerost.api.mission.application.MissionQueryService
import com.zerost.api.mission.application.MissionRewardClaimService
import com.zerost.api.mission.domain.MissionVerificationService
import com.zerost.api.mission.presentation.dto.ClaimMissionRewardResponse
import com.zerost.api.mission.presentation.dto.DailyMissionSectionsResponse
import com.zerost.api.mission.presentation.dto.MissionCompletionHistoryResponse
import com.zerost.api.mission.presentation.dto.MissionDetailResponse
import com.zerost.api.mission.presentation.dto.MissionRewardedIngredientResponse
import com.zerost.api.mission.presentation.dto.MissionSummaryResponse
import com.zerost.api.mission.presentation.dto.MissionTodayStatus
import com.zerost.api.mission.presentation.dto.DeleteMissionVerificationResponse
import com.zerost.api.mission.presentation.dto.SubmitMissionVerificationResponse
import com.zerost.api.mission.presentation.dto.UpdateMissionVerificationResponse
import com.zerost.api.support.createAuthTokenProvider
import com.zerost.api.support.createSubmitMissionVerificationRequestBody
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean

class MissionControllerTest {

    private val missionQueryService = mock(MissionQueryService::class.java)
    private val missionVerificationService = mock(MissionVerificationService::class.java)
    private val missionRewardClaimService = mock(MissionRewardClaimService::class.java)
    private lateinit var mockMvc: MockMvc

    @BeforeEach
    fun setUp() {
        val validator = LocalValidatorFactoryBean().apply { afterPropertiesSet() }
        mockMvc = MockMvcBuilders.standaloneSetup(
            MissionController(missionQueryService, missionVerificationService, missionRewardClaimService),
        )
            .setControllerAdvice(GlobalExceptionHandler())
            .setValidator(validator)
            .addInterceptors(AuthenticationInterceptor(createAuthTokenProvider()))
            .build()
    }

    @Test
    fun `인증된 사용자는 미션 목록을 조회할 수 있다`() {
        `when`(missionQueryService.getMissions(1L)).thenReturn(
            DailyMissionSectionsResponse(
                generalMissions = listOf(
                    MissionSummaryResponse(
                        id = 1L,
                        title = "텀블러 사용하기",
                        description = "설명",
                        imageUrl = "image-1",
                        todayStatus = MissionTodayStatus.PENDING,
                        rewardClaimable = false,
                        rewardClaimed = false,
                        rewardClaimedAt = null,
                    ),
                ),
                specialMission = MissionSummaryResponse(
                    id = 1L,
                    title = "플로깅 인증",
                    description = "설명",
                    imageUrl = "image-1",
                    todayStatus = null,
                    rewardClaimable = false,
                    rewardClaimed = false,
                    rewardClaimedAt = null,
                ),
            ),
        )

        mockMvc.perform(
            get("/api/v1/missions")
                .header("Authorization", "Bearer access-token"),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.generalMissions[0].id").value(1))
            .andExpect(jsonPath("$.data.generalMissions[0].todayStatus").value("PENDING"))
            .andExpect(jsonPath("$.data.specialMission.title").value("플로깅 인증"))

        verify(missionQueryService).getMissions(1L)
    }

    @Test
    fun `인증된 사용자는 미션 상세를 조회할 수 있다`() {
        `when`(missionQueryService.getMission(1L, 1L)).thenReturn(
            MissionDetailResponse(
                id = 1L,
                title = "텀블러 사용하기",
                description = "설명",
                imageUrl = "image-1",
                todayStatus = MissionTodayStatus.APPROVED,
                rewardClaimable = true,
                rewardClaimed = false,
                rewardClaimedAt = null,
            ),
        )

        mockMvc.perform(
            get("/api/v1/missions/1")
                .header("Authorization", "Bearer access-token"),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data.id").value(1))
            .andExpect(jsonPath("$.data.todayStatus").value("APPROVED"))

        verify(missionQueryService).getMission(1L, 1L)
    }

    @Test
    fun `올바른 요청이면 미션 인증 제출 결과를 반환한다`() {
        `when`(
            missionVerificationService.submitVerification(
                1L,
                1L,
                "missions/1/1/2026/07/18/550e8400-e29b-41d4-a716-446655440000.jpg",
            ),
        ).thenReturn(
            SubmitMissionVerificationResponse(
                completionId = 55L,
                status = "PENDING",
            ),
        )

        mockMvc.perform(
            post("/api/v1/missions/1/verify")
                .header("Authorization", "Bearer access-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createSubmitMissionVerificationRequestBody()),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data.completionId").value(55))
            .andExpect(jsonPath("$.data.status").value("PENDING"))

        verify(missionVerificationService).submitVerification(
            1L,
            1L,
            "missions/1/1/2026/07/18/550e8400-e29b-41d4-a716-446655440000.jpg",
        )
    }

    @Test
    fun `photoKey가 비어 있으면 미션 인증 제출에 실패한다`() {
        mockMvc.perform(
            post("/api/v1/missions/1/verify")
                .header("Authorization", "Bearer access-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createSubmitMissionVerificationRequestBody(photoKey = "")),
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("INVALID_INPUT_VALUE"))
    }

    @Test
    fun `올바른 요청이면 미션 인증 수정 결과를 반환한다`() {
        `when`(
            missionVerificationService.updateVerification(
                1L,
                55L,
                "missions/1/1/2026/07/18/550e8400-e29b-41d4-a716-446655440000.jpg",
            ),
        ).thenReturn(
            UpdateMissionVerificationResponse(
                completionId = 55L,
                missionId = 1L,
                status = "PENDING",
                photoKey = "missions/1/1/2026/07/18/550e8400-e29b-41d4-a716-446655440000.jpg",
            ),
        )

        mockMvc.perform(
            patch("/api/v1/missions/completions/55")
                .header("Authorization", "Bearer access-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createSubmitMissionVerificationRequestBody()),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data.completionId").value(55))
            .andExpect(jsonPath("$.data.status").value("PENDING"))

        verify(missionVerificationService).updateVerification(
            1L,
            55L,
            "missions/1/1/2026/07/18/550e8400-e29b-41d4-a716-446655440000.jpg",
        )
    }

    @Test
    fun `인증된 사용자는 미션 인증을 삭제할 수 있다`() {
        `when`(missionVerificationService.deleteVerification(1L, 55L)).thenReturn(
            DeleteMissionVerificationResponse(
                completionId = 55L,
            ),
        )

        mockMvc.perform(
            delete("/api/v1/missions/completions/55")
                .header("Authorization", "Bearer access-token"),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data.completionId").value(55))

        verify(missionVerificationService).deleteVerification(1L, 55L)
    }

    @Test
    fun `인증된 사용자는 내 미션 제출 내역을 조회할 수 있다`() {
        `when`(missionQueryService.getMissionCompletions(1L)).thenReturn(
            listOf(
                MissionCompletionHistoryResponse(
                    completionId = 55L,
                    missionId = 1L,
                    missionTitle = "텀블러 사용하기",
                    status = "APPROVED",
                    rewardClaimable = true,
                    rewardClaimed = false,
                    rewardedIngredient = MissionRewardedIngredientResponse(
                        id = 5L,
                        name = "낡은 밧줄",
                        imageUrl = "image-5",
                    ),
                    submittedAt = "2026-07-17T10:00:00",
                    reviewedAt = "2026-07-17T14:00:00",
                    rewardClaimedAt = null,
                ),
            ),
        )

        mockMvc.perform(
            get("/api/v1/missions/completions")
                .header("Authorization", "Bearer access-token"),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data[0].completionId").value(55))
            .andExpect(jsonPath("$.data[0].rewardedIngredient.id").value(5))
            .andExpect(jsonPath("$.data[0].rewardClaimable").value(true))

        verify(missionQueryService).getMissionCompletions(1L)
    }

    @Test
    fun `인증된 사용자는 승인된 미션 보상을 수령할 수 있다`() {
        `when`(missionRewardClaimService.claimReward(1L, 55L)).thenReturn(
            ClaimMissionRewardResponse(
                completionId = 55L,
                missionId = 1L,
                rewardedIngredient = MissionRewardedIngredientResponse(
                    id = 5L,
                    name = "낡은 밧줄",
                    imageUrl = "image-5",
                ),
                rewardClaimedAt = "2026-07-22T19:30:00",
            ),
        )

        mockMvc.perform(
            post("/api/v1/missions/completions/55/claim")
                .header("Authorization", "Bearer access-token"),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data.completionId").value(55))
            .andExpect(jsonPath("$.data.rewardedIngredient.id").value(5))

        verify(missionRewardClaimService).claimReward(1L, 55L)
    }
}
