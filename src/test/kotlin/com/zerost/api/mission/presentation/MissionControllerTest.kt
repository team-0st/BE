package com.zerost.api.mission.presentation

import com.zerost.api.auth.application.AuthTokenProvider
import com.zerost.api.common.device.DeviceIdInterceptor
import com.zerost.api.common.exception.GlobalExceptionHandler
import com.zerost.api.mission.application.MissionQueryService
import com.zerost.api.mission.domain.MissionVerificationService
import com.zerost.api.mission.presentation.dto.MissionCompletionHistoryResponse
import com.zerost.api.mission.presentation.dto.MissionDetailResponse
import com.zerost.api.mission.presentation.dto.MissionRewardedIngredientResponse
import com.zerost.api.mission.presentation.dto.MissionSummaryResponse
import com.zerost.api.mission.presentation.dto.MissionTodayStatus
import com.zerost.api.mission.presentation.dto.SubmitMissionVerificationResponse
import com.zerost.api.support.createSubmitMissionVerificationRequestBody
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean

class MissionControllerTest {

    private val missionQueryService = mock(MissionQueryService::class.java)
    private val missionVerificationService = mock(MissionVerificationService::class.java)
    private lateinit var mockMvc: MockMvc

    @BeforeEach
    fun setUp() {
        val validator = LocalValidatorFactoryBean().apply { afterPropertiesSet() }
        mockMvc = MockMvcBuilders.standaloneSetup(
            MissionController(missionQueryService, missionVerificationService),
        )
            .setControllerAdvice(GlobalExceptionHandler())
            .setValidator(validator)
            .addInterceptors(DeviceIdInterceptor(mock(AuthTokenProvider::class.java)))
            .build()
    }

    @Test
    fun `디바이스 아이디가 있으면 미션 목록을 조회할 수 있다`() {
        `when`(missionQueryService.getMissions("device-1")).thenReturn(
            listOf(
                MissionSummaryResponse(
                    id = 1L,
                    title = "텀블러 사용하기",
                    description = "설명",
                    imageUrl = "image-1",
                    todayStatus = MissionTodayStatus.PENDING,
                ),
            ),
        )

        mockMvc.perform(
            get("/api/v1/missions")
                .header("X-Device-Id", "device-1"),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data[0].id").value(1))
            .andExpect(jsonPath("$.data[0].todayStatus").value("PENDING"))

        verify(missionQueryService).getMissions("device-1")
    }

    @Test
    fun `디바이스 아이디가 있으면 미션 상세를 조회할 수 있다`() {
        `when`(missionQueryService.getMission("device-1", 1L)).thenReturn(
            MissionDetailResponse(
                id = 1L,
                title = "텀블러 사용하기",
                description = "설명",
                imageUrl = "image-1",
                todayStatus = MissionTodayStatus.APPROVED,
            ),
        )

        mockMvc.perform(
            get("/api/v1/missions/1")
                .header("X-Device-Id", "device-1"),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data.id").value(1))
            .andExpect(jsonPath("$.data.todayStatus").value("APPROVED"))

        verify(missionQueryService).getMission("device-1", 1L)
    }

    @Test
    fun `올바른 요청이면 미션 인증 제출 결과를 반환한다`() {
        `when`(
            missionVerificationService.submitVerification(
                "device-1",
                1L,
                "missions/device-1/1/2026/07/18/550e8400-e29b-41d4-a716-446655440000.jpg",
            ),
        ).thenReturn(
            SubmitMissionVerificationResponse(
                completionId = 55L,
                status = "PENDING",
            ),
        )

        mockMvc.perform(
            post("/api/v1/missions/1/verify")
                .header("X-Device-Id", "device-1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createSubmitMissionVerificationRequestBody()),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data.completionId").value(55))
            .andExpect(jsonPath("$.data.status").value("PENDING"))

        verify(missionVerificationService).submitVerification(
            "device-1",
            1L,
            "missions/device-1/1/2026/07/18/550e8400-e29b-41d4-a716-446655440000.jpg",
        )
    }

    @Test
    fun `photoKey가 비어 있으면 미션 인증 제출에 실패한다`() {
        mockMvc.perform(
            post("/api/v1/missions/1/verify")
                .header("X-Device-Id", "device-1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createSubmitMissionVerificationRequestBody(photoKey = "")),
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("INVALID_INPUT_VALUE"))
    }

    @Test
    fun `디바이스 아이디가 있으면 내 미션 제출 내역을 조회할 수 있다`() {
        `when`(missionQueryService.getMissionCompletions("device-1")).thenReturn(
            listOf(
                MissionCompletionHistoryResponse(
                    completionId = 55L,
                    missionId = 1L,
                    missionTitle = "텀블러 사용하기",
                    status = "APPROVED",
                    rewardedIngredient = MissionRewardedIngredientResponse(
                        id = 5L,
                        name = "낡은 밧줄",
                        imageUrl = "image-5",
                    ),
                    submittedAt = "2026-07-17T10:00:00",
                    reviewedAt = "2026-07-17T14:00:00",
                ),
            ),
        )

        mockMvc.perform(
            get("/api/v1/missions/completions")
                .header("X-Device-Id", "device-1"),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data[0].completionId").value(55))
            .andExpect(jsonPath("$.data[0].rewardedIngredient.id").value(5))

        verify(missionQueryService).getMissionCompletions("device-1")
    }
}
