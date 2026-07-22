package com.zerost.api.communitymission.presentation

import com.zerost.api.common.auth.AuthRequestConstants
import com.zerost.api.common.response.ApiResponse
import com.zerost.api.communitymission.application.AdminCommunityMissionReviewQueryService
import com.zerost.api.communitymission.application.AdminCommunityMissionReviewService
import com.zerost.api.communitymission.presentation.dto.AdminCommunityMissionProofReviewItemResponse
import com.zerost.api.communitymission.presentation.dto.AdminCommunityMissionProofReviewPageResponse
import com.zerost.api.communitymission.presentation.dto.ReviewCommunityMissionProofRequest
import com.zerost.api.communitymission.presentation.dto.ReviewCommunityMissionProofResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse as SwaggerApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
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
        description = "관리자가 검수해야 하는 PENDING 상태의 공동 미션 인증 목록을 제출 시각 오름차순, ID 오름차순 기준으로 페이지 조회합니다.",
    )
    @ApiResponses(
        value = [
            SwaggerApiResponse(responseCode = "200", description = "조회 성공"),
            SwaggerApiResponse(responseCode = "400", description = "유효하지 않은 page 또는 size 값"),
            SwaggerApiResponse(responseCode = "403", description = "관리자 권한이 없는 요청"),
        ],
    )
    @GetMapping("/proofs/pending")
    fun getPendingProofs(
        request: HttpServletRequest,
        @Parameter(description = "페이지 번호(0부터 시작)", example = "0")
        @RequestParam(defaultValue = "0")
        page: Int,
        @Parameter(description = "페이지 크기(최대 100)", example = "20")
        @RequestParam(defaultValue = "20")
        size: Int,
    ): ApiResponse<AdminCommunityMissionProofReviewPageResponse> {
        log.info(
            "admin_pending_community_mission_proofs_requested traceId={} adminUserId={} page={} size={}",
            request.getAttribute(AuthRequestConstants.TRACE_ID_ATTRIBUTE),
            request.getAttribute(AuthRequestConstants.USER_ID_ATTRIBUTE),
            page,
            size,
        )
        return ApiResponse.success(adminCommunityMissionReviewQueryService.getPendingProofs(page, size))
    }

    @Operation(
        summary = "공동 미션 인증 검수 처리",
        description = "관리자가 공동 미션 인증 제출 건을 승인 또는 반려 처리합니다.",
    )
    @ApiResponses(
        value = [
            SwaggerApiResponse(responseCode = "200", description = "검수 처리 성공"),
            SwaggerApiResponse(responseCode = "400", description = "잘못된 요청 또는 허용되지 않은 검수 상태값"),
            SwaggerApiResponse(responseCode = "403", description = "관리자 권한이 없는 요청"),
            SwaggerApiResponse(responseCode = "404", description = "공동 미션 인증 제출 정보를 찾을 수 없음"),
            SwaggerApiResponse(responseCode = "409", description = "이미 검수된 공동 미션 인증은 상태를 변경할 수 없음"),
        ],
    )
    @PostMapping("/proofs/{proofId}/review")
    fun reviewProof(
        @PathVariable proofId: Long,
        @Valid @RequestBody request: ReviewCommunityMissionProofRequest,
        servletRequest: HttpServletRequest,
    ): ApiResponse<ReviewCommunityMissionProofResponse> {
        log.info(
            "admin_community_mission_review_requested traceId={} adminUserId={} proofId={} status={}",
            servletRequest.getAttribute(AuthRequestConstants.TRACE_ID_ATTRIBUTE),
            servletRequest.getAttribute(AuthRequestConstants.USER_ID_ATTRIBUTE),
            proofId,
            request.status,
        )
        return ApiResponse.success(
            adminCommunityMissionReviewService.reviewProof(
                proofId = proofId,
                status = request.status,
            ),
        )
    }

    companion object {
        private val log = LoggerFactory.getLogger(AdminCommunityMissionReviewController::class.java)
    }
}
