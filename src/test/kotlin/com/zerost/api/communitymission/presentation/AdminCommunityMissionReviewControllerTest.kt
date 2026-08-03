package com.zerost.api.communitymission.presentation

import com.zerost.api.common.auth.AdminAuthorizationInterceptor
import com.zerost.api.common.auth.AuthenticationInterceptor
import com.zerost.api.common.auth.RequestTracingInterceptor
import com.zerost.api.common.exception.GlobalExceptionHandler
import com.zerost.api.communitymission.application.AdminCommunityMissionReviewQueryService
import com.zerost.api.communitymission.application.AdminCommunityMissionReviewService
import com.zerost.api.communitymission.presentation.dto.AdminCommunityMissionProofReviewItemResponse
import com.zerost.api.communitymission.presentation.dto.AdminCommunityMissionProofReviewPageResponse
import com.zerost.api.communitymission.presentation.dto.ReviewCommunityMissionProofResponse
import com.zerost.api.support.createAuthTokenProvider
import com.zerost.api.support.createReviewCommunityMissionProofRequestBody
import com.zerost.api.user.domain.UserRole
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

class AdminCommunityMissionReviewControllerTest {
    private val reviewerId = 1L

    private val adminCommunityMissionReviewQueryService = mock(AdminCommunityMissionReviewQueryService::class.java)
    private val adminCommunityMissionReviewService = mock(AdminCommunityMissionReviewService::class.java)
    private lateinit var mockMvc: MockMvc

    @BeforeEach
    fun setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(
            AdminCommunityMissionReviewController(
                adminCommunityMissionReviewQueryService = adminCommunityMissionReviewQueryService,
                adminCommunityMissionReviewService = adminCommunityMissionReviewService,
            ),
        )
            .setControllerAdvice(GlobalExceptionHandler())
            .addInterceptors(
                RequestTracingInterceptor(),
                AuthenticationInterceptor(createAuthTokenProvider(userId = reviewerId, role = UserRole.ADMIN)),
                AdminAuthorizationInterceptor(),
            )
            .build()
    }

    @Test
    fun `관리자는 검수 대기 공동 미션 인증 목록을 조회할 수 있다`() {
        `when`(adminCommunityMissionReviewQueryService.getPendingProofs(0, 20)).thenReturn(
            AdminCommunityMissionProofReviewPageResponse(
                items = listOf(
                    AdminCommunityMissionProofReviewItemResponse(
                        proofId = 101L,
                        communityMissionId = 3L,
                        communityMissionTitle = "7일 일회용컵 없이 생활하기",
                        requirementId = 11L,
                        proofOrder = 1,
                        requirementTitle = "1일차 인증",
                        userId = 1L,
                        nickname = "펭귄탐험가",
                        submittedAt = "2026-07-21T15:30:00",
                        imageKeys = listOf("community-missions/1/3/2026/07/21/proof-1.jpg"),
                        imageUrls = listOf(
                            "https://example.com/community-missions/1/3/2026/07/21/proof-1.jpg?X-Amz-Signature=demo",
                        ),
                    ),
                ),
                page = 0,
                size = 20,
                totalElements = 1,
                totalPages = 1,
                hasNext = false,
            ),
        )

        mockMvc.perform(get("/api/v1/admin/community-missions/proofs/pending").header("Authorization", "Bearer access-token"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data.items[0].proofId").value(101))
            .andExpect(jsonPath("$.data.items[0].communityMissionId").value(3))
            .andExpect(jsonPath("$.data.items[0].requirementId").value(11))
            .andExpect(
                jsonPath("$.data.items[0].imageUrls[0]")
                    .value("https://example.com/community-missions/1/3/2026/07/21/proof-1.jpg?X-Amz-Signature=demo"),
            )
            .andExpect(jsonPath("$.data.page").value(0))
            .andExpect(jsonPath("$.data.size").value(20))
            .andExpect(jsonPath("$.data.totalElements").value(1))
            .andExpect(jsonPath("$.data.totalPages").value(1))
            .andExpect(jsonPath("$.data.hasNext").value(false))

        verify(adminCommunityMissionReviewQueryService).getPendingProofs(0, 20)
    }

    @Test
    fun `관리자는 공동 미션 인증을 검수할 수 있다`() {
        `when`(adminCommunityMissionReviewService.reviewProof(reviewerId, 101L, "APPROVED")).thenReturn(
            ReviewCommunityMissionProofResponse(
                proofId = 101L,
                status = "APPROVED",
                reviewedAt = "2026-07-21T16:00:00",
            ),
        )

        mockMvc.perform(
            post("/api/v1/admin/community-missions/proofs/101/review")
                .header("Authorization", "Bearer access-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createReviewCommunityMissionProofRequestBody("APPROVED")),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data.proofId").value(101))
            .andExpect(jsonPath("$.data.status").value("APPROVED"))

        verify(adminCommunityMissionReviewService).reviewProof(reviewerId, 101L, "APPROVED")
    }

    @Test
    fun `일반 유저는 공동 미션 관리자 검수 목록을 조회할 수 없다`() {
        mockMvc = MockMvcBuilders.standaloneSetup(
            AdminCommunityMissionReviewController(
                adminCommunityMissionReviewQueryService = adminCommunityMissionReviewQueryService,
                adminCommunityMissionReviewService = adminCommunityMissionReviewService,
            ),
        )
            .setControllerAdvice(GlobalExceptionHandler())
            .addInterceptors(
                RequestTracingInterceptor(),
                AuthenticationInterceptor(createAuthTokenProvider(userId = reviewerId, role = UserRole.USER)),
                AdminAuthorizationInterceptor(),
            )
            .build()

        mockMvc.perform(get("/api/v1/admin/community-missions/proofs/pending").header("Authorization", "Bearer access-token"))
            .andExpect(status().isForbidden)
            .andExpect(jsonPath("$.error.code").value("ADMIN_ACCESS_DENIED"))
    }
}
