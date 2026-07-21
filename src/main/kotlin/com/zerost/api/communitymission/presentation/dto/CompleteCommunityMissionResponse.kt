package com.zerost.api.communitymission.presentation.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "공동 미션 완료 처리 응답")
data class CompleteCommunityMissionResponse(

    @Schema(description = "공동 미션 완료 ID", example = "1")
    val completionId: Long,

    @Schema(description = "공동 미션 ID", example = "3")
    val communityMissionId: Long,

    @Schema(description = "지급된 에코잼", example = "50")
    val rewardedEcoJam: Int,

    @Schema(description = "지급된 재료 목록")
    val rewardedIngredients: List<CommunityMissionRewardedIngredientResponse>,

    @Schema(description = "완료 시각", example = "2026-07-21T15:30:00")
    val completedAt: String,
)
