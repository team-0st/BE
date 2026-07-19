package com.zerost.api.mission.presentation

import com.zerost.api.common.response.ApiResponse
import com.zerost.api.mission.application.AdminMissionReviewQueryService
import com.zerost.api.mission.presentation.dto.AdminMissionReviewItemResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse as SwaggerApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Admin Mission", description = "관리자 미션 검수 API")
@RestController
@RequestMapping("/api/v1/admin/missions")
class AdminMissionReviewController(
    private val adminMissionReviewQueryService: AdminMissionReviewQueryService,
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
}
