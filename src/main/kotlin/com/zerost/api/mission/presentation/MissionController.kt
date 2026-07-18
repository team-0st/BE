package com.zerost.api.mission.presentation

import com.zerost.api.common.device.DeviceConstants
import com.zerost.api.common.response.ApiResponse
import com.zerost.api.mission.application.MissionQueryService
import com.zerost.api.mission.domain.MissionVerificationService
import com.zerost.api.mission.presentation.dto.MissionCompletionHistoryResponse
import com.zerost.api.mission.presentation.dto.MissionDetailResponse
import com.zerost.api.mission.presentation.dto.MissionSummaryResponse
import com.zerost.api.mission.presentation.dto.SubmitMissionVerificationRequest
import com.zerost.api.mission.presentation.dto.SubmitMissionVerificationResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse as SwaggerApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Mission", description = "미션 조회 및 제출 API")
@RestController
@RequestMapping("/api/v1/missions")
class MissionController(
    private val missionQueryService: MissionQueryService,
    private val missionVerificationService: MissionVerificationService
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

    @Operation(
        summary = "미션 인증 제출",
        description = "파일 업로드 API로 업로드한 인증 사진 URL을 받아 미션 인증을 제출하고 검수 대기 상태로 저장합니다.",
    )
    @ApiResponses(
        value = [
            SwaggerApiResponse(responseCode = "200", description = "제출 성공"),
            SwaggerApiResponse(responseCode = "400", description = "잘못된 요청"),
            SwaggerApiResponse(responseCode = "404", description = "등록된 유저 또는 미션을 찾을 수 없음"),
            SwaggerApiResponse(responseCode = "409", description = "오늘 이미 검수 중이거나 승인된 미션입니다."),
        ],
    )
    @PostMapping("/{missionId}/verify")
    fun submitVerification(
        @PathVariable missionId: Long,
        @Valid @RequestBody request: SubmitMissionVerificationRequest,
        httpServletRequest: HttpServletRequest,
    ): ApiResponse<SubmitMissionVerificationResponse> {
        val deviceId = httpServletRequest.getAttribute(DeviceConstants.DEVICE_ID_ATTRIBUTE) as String
        val response = missionVerificationService.submitVerification(
            deviceId = deviceId,
            missionId = missionId,
            photoUrl = request.photoUrl,
        )
        return ApiResponse.success(response)
    }

    @Operation(
        summary = "내 미션 제출 내역 조회",
        description = "내가 제출한 미션 인증 내역과 검수 결과를 조회합니다.",
    )
    @ApiResponses(
        value = [
            SwaggerApiResponse(responseCode = "200", description = "조회 성공"),
            SwaggerApiResponse(responseCode = "404", description = "등록된 유저를 찾을 수 없음"),
        ],
    )
    @GetMapping("/completions")
    fun getMissionCompletions(
        httpServletRequest: HttpServletRequest,
    ): ApiResponse<List<MissionCompletionHistoryResponse>> {
        val deviceId = httpServletRequest.getAttribute(DeviceConstants.DEVICE_ID_ATTRIBUTE) as String
        val response = missionQueryService.getMissionCompletions(deviceId)
        return ApiResponse.success(response)
    }
}
