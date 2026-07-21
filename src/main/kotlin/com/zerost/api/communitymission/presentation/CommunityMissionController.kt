package com.zerost.api.communitymission.presentation

import com.zerost.api.common.device.DeviceConstants
import com.zerost.api.common.response.ApiResponse
import com.zerost.api.communitymission.application.CommunityMissionCompletionService
import com.zerost.api.communitymission.application.CommunityMissionQueryService
import com.zerost.api.communitymission.presentation.dto.CompleteCommunityMissionResponse
import com.zerost.api.communitymission.presentation.dto.CommunityMissionProgressResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse as SwaggerApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletRequest
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "CommunityMission", description = "공동 미션 조회 및 완료 API")
@RestController
@RequestMapping("/api/v1/community-missions")
class CommunityMissionController(
    private val communityMissionQueryService: CommunityMissionQueryService,
    private val communityMissionCompletionService: CommunityMissionCompletionService,
) {

    @Operation(
        summary = "공동 미션 진행률 조회",
        description = "전체 유저 대비 달성 비율과 현재 유저 기준 해금 여부를 포함한 공동 미션 목록을 조회합니다.",
    )
    @ApiResponses(
        value = [
            SwaggerApiResponse(responseCode = "200", description = "조회 성공"),
            SwaggerApiResponse(responseCode = "404", description = "등록된 유저를 찾을 수 없음"),
        ],
    )
    @GetMapping
    fun getCommunityMissions(
        httpServletRequest: HttpServletRequest,
    ): ApiResponse<List<CommunityMissionProgressResponse>> {
        val deviceId = httpServletRequest.getAttribute(DeviceConstants.DEVICE_ID_ATTRIBUTE) as String
        val response = communityMissionQueryService.getCommunityMissions(deviceId)
        return ApiResponse.success(response)
    }

    @Operation(
        summary = "공동 미션 완료 처리",
        description = "현재 유저가 해금한 공동 미션을 완료 처리하고 진행률 집계 대상에 반영합니다.",
    )
    @ApiResponses(
        value = [
            SwaggerApiResponse(responseCode = "200", description = "완료 처리 성공"),
            SwaggerApiResponse(responseCode = "404", description = "등록된 유저 또는 공동 미션을 찾을 수 없음"),
            SwaggerApiResponse(responseCode = "409", description = "온보딩 미완료, 미해금 공동 미션, 이미 완료한 공동 미션"),
        ],
    )
    @PostMapping("/{communityMissionId}/complete")
    fun completeCommunityMission(
        @PathVariable communityMissionId: Long,
        httpServletRequest: HttpServletRequest,
    ): ApiResponse<CompleteCommunityMissionResponse> {
        val deviceId = httpServletRequest.getAttribute(DeviceConstants.DEVICE_ID_ATTRIBUTE) as String
        val response = communityMissionCompletionService.complete(deviceId, communityMissionId)
        return ApiResponse.success(response)
    }
}
