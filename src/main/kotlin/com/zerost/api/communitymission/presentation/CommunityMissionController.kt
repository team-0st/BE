package com.zerost.api.communitymission.presentation

import com.zerost.api.common.auth.AuthRequestConstants
import com.zerost.api.common.response.ApiResponse
import com.zerost.api.communitymission.application.CommunityMissionCompletionService
import com.zerost.api.communitymission.application.CommunityMissionProofService
import com.zerost.api.communitymission.application.CommunityMissionQueryService
import com.zerost.api.communitymission.presentation.dto.CompleteCommunityMissionResponse
import com.zerost.api.communitymission.presentation.dto.CommunityMissionDetailResponse
import com.zerost.api.communitymission.presentation.dto.CommunityMissionProgressResponse
import com.zerost.api.communitymission.presentation.dto.SubmitCommunityMissionProofRequest
import com.zerost.api.communitymission.presentation.dto.SubmitCommunityMissionProofResponse
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

@Tag(name = "CommunityMission", description = "공동 미션 조회, 인증 제출, 완료 API")
@RestController
@RequestMapping("/api/v1/community-missions")
class CommunityMissionController(
    private val communityMissionQueryService: CommunityMissionQueryService,
    private val communityMissionProofService: CommunityMissionProofService,
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
        val userId = httpServletRequest.getAttribute(AuthRequestConstants.USER_ID_ATTRIBUTE) as Long
        val response = communityMissionQueryService.getCommunityMissions(userId)
        return ApiResponse.success(response)
    }

    @Operation(
        summary = "공동 미션 상세 조회",
        description = "공동 미션 인증 단계와 현재 유저의 제출 현황을 포함한 상세 정보를 조회합니다.",
    )
    @ApiResponses(
        value = [
            SwaggerApiResponse(responseCode = "200", description = "조회 성공"),
            SwaggerApiResponse(responseCode = "404", description = "등록된 유저 또는 공동 미션을 찾을 수 없음"),
        ],
    )
    @GetMapping("/{communityMissionId}")
    fun getCommunityMission(
        @PathVariable communityMissionId: Long,
        httpServletRequest: HttpServletRequest,
    ): ApiResponse<CommunityMissionDetailResponse> {
        val userId = httpServletRequest.getAttribute(AuthRequestConstants.USER_ID_ATTRIBUTE) as Long
        val response = communityMissionQueryService.getCommunityMission(userId, communityMissionId)
        return ApiResponse.success(response)
    }

    @Operation(
        summary = "공동 미션 인증 제출",
        description = "공동 미션의 특정 인증 단계에 필요한 이미지들을 제출합니다.",
    )
    @ApiResponses(
        value = [
            SwaggerApiResponse(responseCode = "200", description = "제출 성공"),
            SwaggerApiResponse(responseCode = "400", description = "이미지 수 불일치 또는 잘못된 요청"),
            SwaggerApiResponse(responseCode = "404", description = "등록된 유저, 공동 미션 또는 인증 단계를 찾을 수 없음"),
            SwaggerApiResponse(responseCode = "409", description = "온보딩 미완료, 미해금 공동 미션, 이미 완료한 공동 미션, 이미 제출한 인증 단계"),
        ],
    )
    @PostMapping("/{communityMissionId}/proofs/{requirementId}")
    fun submitProof(
        @PathVariable communityMissionId: Long,
        @PathVariable requirementId: Long,
        @Valid @RequestBody request: SubmitCommunityMissionProofRequest,
        httpServletRequest: HttpServletRequest,
    ): ApiResponse<SubmitCommunityMissionProofResponse> {
        val userId = httpServletRequest.getAttribute(AuthRequestConstants.USER_ID_ATTRIBUTE) as Long
        val response = communityMissionProofService.submitProof(
            userId = userId,
            communityMissionId = communityMissionId,
            requirementId = requirementId,
            photoKeys = request.photoKeys,
        )
        return ApiResponse.success(response)
    }

    @Operation(
        summary = "공동 미션 완료 처리",
        description = "현재 유저가 필요한 인증 단계를 모두 제출한 뒤 공동 미션을 완료 처리하고 진행률 집계 대상에 반영한 뒤, 설정된 공동 미션 보상을 지급합니다.",
    )
    @ApiResponses(
        value = [
            SwaggerApiResponse(responseCode = "200", description = "완료 처리 성공"),
            SwaggerApiResponse(responseCode = "404", description = "등록된 유저, 공동 미션 또는 인증 단계 정보를 찾을 수 없음"),
            SwaggerApiResponse(responseCode = "409", description = "온보딩 미완료, 미해금 공동 미션, 이미 완료한 공동 미션, 필수 인증 단계 미제출"),
        ],
    )
    @PostMapping("/{communityMissionId}/complete")
    fun completeCommunityMission(
        @PathVariable communityMissionId: Long,
        httpServletRequest: HttpServletRequest,
    ): ApiResponse<CompleteCommunityMissionResponse> {
        val userId = httpServletRequest.getAttribute(AuthRequestConstants.USER_ID_ATTRIBUTE) as Long
        val response = communityMissionCompletionService.complete(userId, communityMissionId)
        return ApiResponse.success(response)
    }
}
