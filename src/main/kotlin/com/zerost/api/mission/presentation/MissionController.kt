package com.zerost.api.mission.presentation

import com.zerost.api.common.device.DeviceConstants
import com.zerost.api.common.response.ApiResponse
import com.zerost.api.mission.application.MissionQueryService
import com.zerost.api.mission.presentation.dto.MissionDetailResponse
import com.zerost.api.mission.presentation.dto.MissionSummaryResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse as SwaggerApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletRequest
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Mission", description = "미션 조회 및 제출 API")
@RestController
@RequestMapping("/api/v1/missions")
class MissionController(
    private val missionQueryService: MissionQueryService,
) {

    @Operation(
        summary = "미션 목록 조회",
        description = "오늘 상태를 포함한 미션 목록을 조회합니다.",
    )
    @ApiResponses(
        value = [
            SwaggerApiResponse(responseCode = "200", description = "조회 성공"),
            SwaggerApiResponse(responseCode = "404", description = "등록된 유저를 찾을 수 없음"),
        ],
    )
    @GetMapping
    fun getMissions(
        httpServletRequest: HttpServletRequest,
    ): ApiResponse<List<MissionSummaryResponse>> {
        val deviceId = httpServletRequest.getAttribute(DeviceConstants.DEVICE_ID_ATTRIBUTE) as String
        val response = missionQueryService.getMissions(deviceId)
        return ApiResponse.success(response)
    }

    @Operation(
        summary = "미션 상세 조회",
        description = "선택한 미션 상세 정보와 오늘 상태를 조회합니다.",
    )
    @ApiResponses(
        value = [
            SwaggerApiResponse(responseCode = "200", description = "조회 성공"),
            SwaggerApiResponse(responseCode = "404", description = "등록된 유저 또는 미션을 찾을 수 없음"),
        ],
    )
    @GetMapping("/{missionId}")
    fun getMission(
        @PathVariable missionId: Long,
        httpServletRequest: HttpServletRequest,
    ): ApiResponse<MissionDetailResponse> {
        val deviceId = httpServletRequest.getAttribute(DeviceConstants.DEVICE_ID_ATTRIBUTE) as String
        val response = missionQueryService.getMission(deviceId, missionId)
        return ApiResponse.success(response)
    }
}
