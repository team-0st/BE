package com.zerost.api.mission.presentation

import com.zerost.api.common.auth.AuthRequestConstants
import com.zerost.api.common.response.ApiResponse
import com.zerost.api.mission.application.AdminMissionReviewQueryService
import com.zerost.api.mission.application.AdminMissionReviewService
import com.zerost.api.mission.presentation.dto.AdminMissionReviewItemResponse
import com.zerost.api.mission.presentation.dto.ReviewMissionCompletionRequest
import com.zerost.api.mission.presentation.dto.ReviewMissionCompletionResponse
import io.swagger.v3.oas.annotations.Operation
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
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Admin Mission", description = "관리자 미션 검수 API")
@RestController
@RequestMapping("/api/v1/admin/missions")
class AdminMissionReviewController(
    private val adminMissionReviewQueryService: AdminMissionReviewQueryService,
    private val adminMissionReviewService: AdminMissionReviewService,
) {

    @Operation(
        summary = "검수 대기 미션 인증 목록 조회",
        description = "관리자가 검수해야 하는 PENDING 상태의 미션 인증 목록을 제출 시각 오름차순으로 조회합니다.",
    )
    @ApiResponses(
        value = [
            SwaggerApiResponse(responseCode = "200", description = "조회 성공"),
            SwaggerApiResponse(responseCode = "403", description = "관리자 권한이 없는 요청"),
        ],
    )
    @GetMapping("/completions/pending")
    fun getPendingMissionCompletions(request: HttpServletRequest): ApiResponse<List<AdminMissionReviewItemResponse>> {
        log.info(
            "admin_pending_mission_completions_requested traceId={} adminUserId={}",
            request.getAttribute(AuthRequestConstants.TRACE_ID_ATTRIBUTE),
            request.getAttribute(AuthRequestConstants.USER_ID_ATTRIBUTE),
        )
        val response = adminMissionReviewQueryService.getPendingMissionCompletions()
        return ApiResponse.success(response)
    }

    @Operation(
        summary = "미션 인증 검수 처리",
        description = "관리자가 미션 인증 제출 건을 승인 또는 반려 처리합니다. completionId는 미션 ID가 아니라 미션 인증 제출 ID이며, APPROVED 처리 시 미션의 보상 재료 풀 기준으로 재료를 지급합니다. 일반 미션은 보상 풀 내 랜덤 일반 재료를, 특별 미션은 단일 보상 풀 기준 고정 히든 재료를 지급합니다.",
    )
    @ApiResponses(
        value = [
            SwaggerApiResponse(responseCode = "200", description = "검수 처리 성공"),
            SwaggerApiResponse(responseCode = "400", description = "잘못된 요청 또는 허용되지 않은 검수 상태값"),
            SwaggerApiResponse(responseCode = "403", description = "관리자 권한이 없는 요청"),
            SwaggerApiResponse(responseCode = "404", description = "미션 인증 정보를 찾을 수 없음"),
            SwaggerApiResponse(responseCode = "409", description = "이미 검수된 미션 인증으로 상태를 변경할 수 없음"),
        ],
    )
    @PostMapping("/completions/{completionId}/review")
    fun reviewMissionCompletion(
        @PathVariable completionId: Long,
        @Valid @RequestBody request: ReviewMissionCompletionRequest,
        servletRequest: HttpServletRequest,
    ): ApiResponse<ReviewMissionCompletionResponse> {
        log.info(
            "admin_mission_review_requested traceId={} adminUserId={} completionId={} status={}",
            servletRequest.getAttribute(AuthRequestConstants.TRACE_ID_ATTRIBUTE),
            servletRequest.getAttribute(AuthRequestConstants.USER_ID_ATTRIBUTE),
            completionId,
            request.status,
        )
        val reviewerId = servletRequest.getAttribute(AuthRequestConstants.USER_ID_ATTRIBUTE) as Long
        val response = adminMissionReviewService.reviewMissionCompletion(
            reviewerId = reviewerId,
            completionId = completionId,
            status = request.status,
        )
        return ApiResponse.success(response)
    }

    companion object {
        private val log = LoggerFactory.getLogger(AdminMissionReviewController::class.java)
    }
}
