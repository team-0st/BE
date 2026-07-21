package com.zerost.api.mission.presentation

import com.zerost.api.common.exception.GlobalExceptionHandler
import com.zerost.api.mission.application.AdminMissionReviewQueryService
import com.zerost.api.mission.application.AdminMissionReviewService
import com.zerost.api.mission.presentation.dto.AdminMissionReviewItemResponse
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

class AdminMissionReviewControllerTest {

    private val adminMissionReviewQueryService = mock(AdminMissionReviewQueryService::class.java)
    private val adminMissionReviewService = mock(AdminMissionReviewService::class.java)
    private lateinit var mockMvc: MockMvc

    @BeforeEach
    fun setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(
            AdminMissionReviewController(
                adminMissionReviewQueryService,
                adminMissionReviewService,
            ),
        )
            .setControllerAdvice(GlobalExceptionHandler())
            .build()
    }

    @Test
    fun `검수 대기 미션 인증 목록을 조회할 수 있다`() {
        `when`(adminMissionReviewQueryService.getPendingMissionCompletions()).thenReturn(
            listOf(
                AdminMissionReviewItemResponse(
                    completionId = 12L,
                    userId = 3L,
                    userNickname = "펭귄탐험가",
                    missionId = 1L,
                    missionTitle = "텀블러 사용하기",
                    photoKey = "missions/1/1/2026/07/18/file.jpg",
                    submittedAt = "2026-07-18T10:00:00",
                ),
            ),
        )

        mockMvc.perform(get("/api/v1/admin/missions/completions/pending"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data[0].completionId").value(12))
            .andExpect(jsonPath("$.data[0].missionTitle").value("텀블러 사용하기"))

        verify(adminMissionReviewQueryService).getPendingMissionCompletions()
    }
}
