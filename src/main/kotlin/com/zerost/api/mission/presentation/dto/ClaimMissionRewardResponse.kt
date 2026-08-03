package com.zerost.api.mission.presentation.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "미션 보상 수령 응답")
data class ClaimMissionRewardResponse(

    @field:Schema(description = "미션 인증 ID", example = "55")
    val completionId: Long,

    @field:Schema(description = "미션 ID", example = "1")
    val missionId: Long,

    @field:Schema(description = "수령한 보상 재료")
    val rewardedIngredient: MissionRewardedIngredientResponse,

    @field:Schema(description = "보상 수령 시각", example = "2026-07-22T19:30:00")
    val rewardClaimedAt: String,
)
