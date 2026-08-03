package com.zerost.api.reward.presentation

import com.zerost.api.common.auth.AuthRequestConstants
import com.zerost.api.common.response.ApiResponse
import com.zerost.api.reward.application.RewardClaimService
import com.zerost.api.reward.application.RewardQueryService
import com.zerost.api.reward.presentation.dto.ClaimAllRewardsResponse
import com.zerost.api.reward.presentation.dto.ClaimRewardResponse
import com.zerost.api.reward.presentation.dto.RewardsTabResponse
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

@Tag(name = "Reward", description = "미수령 보상 탭 API")
@RestController
@RequestMapping("/api/v1/rewards")
class RewardController(
    private val rewardQueryService: RewardQueryService,
    private val rewardClaimService: RewardClaimService,
) {

    @Operation(
        summary = "보상 탭 조회",
        description = "아직 수령하지 않은 보상을 요약과 목록으로 조회합니다. v1은 일반 미션 미수령 보상만 포함합니다.",
    )
    @ApiResponses(
        value = [
            SwaggerApiResponse(responseCode = "200", description = "조회 성공"),
            SwaggerApiResponse(responseCode = "404", description = "등록된 유저를 찾을 수 없음"),
        ],
    )
    @GetMapping
    fun getRewards(httpServletRequest: HttpServletRequest): ApiResponse<RewardsTabResponse> {
        val userId = httpServletRequest.getAttribute(AuthRequestConstants.USER_ID_ATTRIBUTE) as Long
        return ApiResponse.success(rewardQueryService.getRewards(userId))
    }

    @Operation(
        summary = "보상 개별 수령",
        description = "특정 보상 묶음 1개를 수령합니다. v1에서 MISSION 보상은 rewardId = mission completionId 입니다.",
    )
    @ApiResponses(
        value = [
            SwaggerApiResponse(responseCode = "200", description = "수령 성공"),
            SwaggerApiResponse(responseCode = "404", description = "보상 또는 유저를 찾을 수 없음"),
            SwaggerApiResponse(responseCode = "409", description = "수령 불가 또는 이미 수령함"),
        ],
    )
    @PostMapping("/{rewardId}/claim")
    fun claimReward(
        @PathVariable rewardId: Long,
        httpServletRequest: HttpServletRequest,
    ): ApiResponse<ClaimRewardResponse> {
        val userId = httpServletRequest.getAttribute(AuthRequestConstants.USER_ID_ATTRIBUTE) as Long
        return ApiResponse.success(rewardClaimService.claimReward(userId, rewardId))
    }

    @Operation(
        summary = "보상 일괄 수령",
        description = "아직 수령하지 않은 모든 보상을 한 번에 수령합니다.",
    )
    @ApiResponses(
        value = [
            SwaggerApiResponse(responseCode = "200", description = "일괄 수령 성공"),
            SwaggerApiResponse(responseCode = "404", description = "등록된 유저를 찾을 수 없음"),
        ],
    )
    @PostMapping("/claim-all")
    fun claimAll(httpServletRequest: HttpServletRequest): ApiResponse<ClaimAllRewardsResponse> {
        val userId = httpServletRequest.getAttribute(AuthRequestConstants.USER_ID_ATTRIBUTE) as Long
        return ApiResponse.success(rewardClaimService.claimAll(userId))
    }
}
