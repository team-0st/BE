package com.zerost.api.mission.presentation

import com.zerost.api.common.exception.GlobalExceptionHandler
import com.zerost.api.mission.application.AdminMissionReviewQueryService
import com.zerost.api.mission.application.AdminMissionReviewService
import com.zerost.api.mission.presentation.dto.ReviewMissionCompletionResponse
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

class AdminMissionReviewControllerActionTest {

    private val adminMissionReviewQueryService = mock(AdminMissionReviewQueryService::class.java)
    private val adminMissionReviewService = mock(AdminMissionReviewService::class.java)
    private lateinit var mockMvc: MockMvc

    @BeforeEach
    fun setUp() {
        val validator = LocalValidatorFactoryBean().apply { afterPropertiesSet() }

        mockMvc = MockMvcBuilders.standaloneSetup(
            AdminMissionReviewController(
                adminMissionReviewQueryService,
                adminMissionReviewService,
            ),
        )
            .setControllerAdvice(GlobalExceptionHandler())
            .setValidator(validator)
            .build()
    }

    @Test
    fun `미션 인증을 승인 처리할 수 있다`() {
        `when`(adminMissionReviewService.reviewMissionCompletion(12L, "APPROVED")).thenReturn(
            ReviewMissionCompletionResponse(
                completionId = 12L,
                status = "APPROVED",
                reviewedAt = "2026-07-19T14:30:00",
            ),
        )

        mockMvc.perform(
            post("/api/v1/admin/missions/completions/12/review")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"status":"APPROVED"}"""),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.status").value("APPROVED"))

        verify(adminMissionReviewService).reviewMissionCompletion(12L, "APPROVED")
    }

    @Test
    fun `status가 비어 있으면 검수 처리에 실패한다`() {
        mockMvc.perform(
            post("/api/v1/admin/missions/completions/12/review")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"status":""}"""),
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("INVALID_INPUT_VALUE"))
    }
}
