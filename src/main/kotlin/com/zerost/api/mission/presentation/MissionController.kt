package com.zerost.api.mission.presentation

import com.zerost.api.common.auth.AuthRequestConstants
import com.zerost.api.common.response.ApiResponse
import com.zerost.api.mission.application.MissionQueryService
import com.zerost.api.mission.domain.MissionVerificationService
import com.zerost.api.mission.presentation.dto.DailyMissionSectionsResponse
import com.zerost.api.mission.presentation.dto.DeleteMissionVerificationResponse
import com.zerost.api.mission.presentation.dto.MissionCompletionHistoryResponse
import com.zerost.api.mission.presentation.dto.MissionDetailResponse
import com.zerost.api.mission.presentation.dto.SubmitMissionVerificationRequest
import com.zerost.api.mission.presentation.dto.SubmitMissionVerificationResponse
import com.zerost.api.mission.presentation.dto.UpdateMissionVerificationResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse as SwaggerApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.DeleteMapping

@Tag(name = "Mission", description = "미션 조회 및 제출 API")
@RestController
@RequestMapping("/api/v1/missions")
class MissionController(
    private val missionQueryService: MissionQueryService,
    private val missionVerificationService: MissionVerificationService
) {

    @Operation(
        summary = "미션 목록 조회",
        description = "일반 미션 3개와 특별 미션 1개를 오늘 기준 고정 편성 결과로 조회합니다.",
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
    ): ApiResponse<DailyMissionSectionsResponse> {
        val userId = httpServletRequest.getAttribute(AuthRequestConstants.USER_ID_ATTRIBUTE) as Long
        val response = missionQueryService.getMissions(userId)
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
        val userId = httpServletRequest.getAttribute(AuthRequestConstants.USER_ID_ATTRIBUTE) as Long
        val response = missionQueryService.getMission(userId, missionId)
        return ApiResponse.success(response)
    }

    @Operation(
        summary = "미션 인증 제출",
        description = "파일 업로드 API로 업로드한 인증 이미지 파일 키를 받아 미션 인증을 제출하고 검수 대기 상태로 저장합니다.",
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
        val userId = httpServletRequest.getAttribute(AuthRequestConstants.USER_ID_ATTRIBUTE) as Long
        val response = missionVerificationService.submitVerification(
            userId = userId,
            missionId = missionId,
            photoKey = request.photoKey,
        )
        return ApiResponse.success(response)
    }

    @Operation(
        summary = "미션 인증 수정",
        description = "검수 대기 또는 반려 상태의 미션 인증 이미지를 수정합니다.",
    )
    @ApiResponses(
        value = [
            SwaggerApiResponse(responseCode = "200", description = "수정 성공"),
            SwaggerApiResponse(responseCode = "400", description = "잘못된 요청"),
            SwaggerApiResponse(responseCode = "404", description = "등록된 유저 또는 미션 인증을 찾을 수 없음"),
            SwaggerApiResponse(responseCode = "409", description = "승인된 미션 인증은 수정할 수 없습니다."),
        ],
    )
    @PatchMapping("/completions/{completionId}")
    fun updateVerification(
        @PathVariable completionId: Long,
        @Valid @RequestBody request: SubmitMissionVerificationRequest,
        httpServletRequest: HttpServletRequest,
    ): ApiResponse<UpdateMissionVerificationResponse> {
        val userId = httpServletRequest.getAttribute(AuthRequestConstants.USER_ID_ATTRIBUTE) as Long
        val response = missionVerificationService.updateVerification(
            userId = userId,
            completionId = completionId,
            photoKey = request.photoKey,
        )
        return ApiResponse.success(response)
    }

    @Operation(
        summary = "미션 인증 삭제",
        description = "검수 대기 또는 반려 상태의 미션 인증을 삭제합니다.",
    )
    @ApiResponses(
        value = [
            SwaggerApiResponse(responseCode = "200", description = "삭제 성공"),
            SwaggerApiResponse(responseCode = "404", description = "등록된 유저 또는 미션 인증을 찾을 수 없음"),
            SwaggerApiResponse(responseCode = "409", description = "승인된 미션 인증은 삭제할 수 없습니다."),
        ],
    )
    @DeleteMapping("/completions/{completionId}")
    fun deleteVerification(
        @PathVariable completionId: Long,
        httpServletRequest: HttpServletRequest,
    ): ApiResponse<DeleteMissionVerificationResponse> {
        val userId = httpServletRequest.getAttribute(AuthRequestConstants.USER_ID_ATTRIBUTE) as Long
        val response = missionVerificationService.deleteVerification(
            userId = userId,
            completionId = completionId,
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
        val userId = httpServletRequest.getAttribute(AuthRequestConstants.USER_ID_ATTRIBUTE) as Long
        val response = missionQueryService.getMissionCompletions(userId)
        return ApiResponse.success(response)
    }
}
