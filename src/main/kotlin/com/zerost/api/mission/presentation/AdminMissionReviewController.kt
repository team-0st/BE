package com.zerost.api.mission.presentation

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
import jakarta.validation.Valid
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
        description = "관리자가 검수해야 하는 미션 인증 목록을 제출 시각 오름차순으로 조회합니다.",
    )
    @ApiResponses(
        value = [
            SwaggerApiResponse(responseCode = "200", description = "조회 성공"),
        ],
    )
    @GetMapping("/completions/pending")
    fun getPendingMissionCompletions(): ApiResponse<List<AdminMissionReviewItemResponse>> {
        val response = adminMissionReviewQueryService.getPendingMissionCompletions()
        return ApiResponse.success(response)
    }

    @Operation(
        summary = "미션 인증 검수 처리",
        description = "관리자가 미션 인증 건을 승인 또는 반려 처리합니다.",
    )
    @ApiResponses(
        value = [
            SwaggerApiResponse(responseCode = "200", description = "검수 처리 성공"),
            SwaggerApiResponse(responseCode = "400", description = "잘못된 요청"),
            SwaggerApiResponse(responseCode = "404", description = "미션 인증 정보를 찾을 수 없음"),
            SwaggerApiResponse(responseCode = "409", description = "검수할 수 없는 미션 인증 상태"),
        ],
    )
    @PostMapping("/completions/{completionId}/review")
    fun reviewMissionCompletion(
        @PathVariable completionId: Long,
        @Valid @RequestBody request: ReviewMissionCompletionRequest,
    ): ApiResponse<ReviewMissionCompletionResponse> {
        val response = adminMissionReviewService.reviewMissionCompletion(
            completionId = completionId,
            status = request.status,
        )
        return ApiResponse.success(response)
    }
}
