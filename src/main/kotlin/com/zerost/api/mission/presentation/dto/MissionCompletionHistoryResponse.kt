package com.zerost.api.mission.presentation.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "미션 제출 내역 응답 항목")
data class MissionCompletionHistoryResponse(

    @Schema(description = "미션 제출 ID", example = "55")
    val completionId: Long,

    @Schema(description = "미션 ID", example = "1")
    val missionId: Long,

    @Schema(description = "미션 제목", example = "텀블러 사용하기")
    val missionTitle: String,

    @Schema(description = "제출 상태", example = "APPROVED")
    val status: String,

    @Schema(description = "보상 수령 가능 여부", example = "true")
    val rewardClaimable: Boolean,

    @Schema(description = "보상 수령 여부", example = "false")
    val rewardClaimed: Boolean,

    @Schema(description = "보상 재료 정보", nullable = true)
    val rewardedIngredient: MissionRewardedIngredientResponse?,

    @Schema(description = "제출 시각", example = "2026-07-17T10:00:00")
    val submittedAt: String,

    @Schema(description = "검수 시각", example = "2026-07-17T14:00:00", nullable = true)
    val reviewedAt: String?,

    @Schema(description = "보상 수령 시각", example = "2026-07-17T14:10:00", nullable = true)
    val rewardClaimedAt: String?,
)
