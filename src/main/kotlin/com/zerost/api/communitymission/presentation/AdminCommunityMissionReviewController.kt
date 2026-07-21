package com.zerost.api.communitymission.presentation

import com.zerost.api.common.response.ApiResponse
import com.zerost.api.communitymission.application.AdminCommunityMissionReviewQueryService
import com.zerost.api.communitymission.application.AdminCommunityMissionReviewService
import com.zerost.api.communitymission.presentation.dto.AdminCommunityMissionProofReviewItemResponse
import com.zerost.api.communitymission.presentation.dto.ReviewCommunityMissionProofRequest
import com.zerost.api.communitymission.presentation.dto.ReviewCommunityMissionProofResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse as SwaggerApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Admin Community Mission", description = "관리자 공동 미션 검수 API")
@RestController
@RequestMapping("/api/v1/admin/community-missions")
class AdminCommunityMissionReviewController(
    private val adminCommunityMissionReviewQueryService: AdminCommunityMissionReviewQueryService,
    private val adminCommunityMissionReviewService: AdminCommunityMissionReviewService,
) {

    @Operation(
        summary = "검수 대기 공동 미션 인증 목록 조회",
        description = "관리자가 검수해야 하는 PENDING 상태의 공동 미션 인증 목록을 제출 시각 오름차순으로 조회합니다.",
    )
    @ApiResponses(
        value = [
            SwaggerApiResponse(responseCode = "200", description = "조회 성공"),
        ],
    )
    @GetMapping("/proofs/pending")
    fun getPendingProofs(): ApiResponse<List<AdminCommunityMissionProofReviewItemResponse>> {
        return ApiResponse.success(adminCommunityMissionReviewQueryService.getPendingProofs())
    }

    @Operation(
        summary = "공동 미션 인증 검수 처리",
        description = "관리자가 공동 미션 인증 제출 건을 승인 또는 반려 처리합니다.",
    )
    @ApiResponses(
        value = [
            SwaggerApiResponse(responseCode = "200", description = "검수 처리 성공"),
            SwaggerApiResponse(responseCode = "400", description = "잘못된 요청 또는 허용되지 않은 검수 상태값"),
            SwaggerApiResponse(responseCode = "404", description = "공동 미션 인증 제출 정보를 찾을 수 없음"),
            SwaggerApiResponse(responseCode = "409", description = "이미 검수된 공동 미션 인증은 상태를 변경할 수 없음"),
        ],
    )
    @PostMapping("/proofs/{proofId}/review")
    fun reviewProof(
        @PathVariable proofId: Long,
        @Valid @RequestBody request: ReviewCommunityMissionProofRequest,
    ): ApiResponse<ReviewCommunityMissionProofResponse> {
        return ApiResponse.success(
            adminCommunityMissionReviewService.reviewProof(
                proofId = proofId,
                status = request.status,
            ),
        )
    }
}
