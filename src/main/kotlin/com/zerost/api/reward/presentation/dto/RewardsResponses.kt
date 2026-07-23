package com.zerost.api.reward.presentation.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "보상 탭 조회 응답")
data class RewardsTabResponse(
    @field:Schema(description = "미수령 보상 요약")
    val summary: RewardsSummaryResponse,
    @field:Schema(description = "미수령 보상 묶음 목록")
    val items: List<RewardBundleResponse>,
)

@Schema(description = "미수령 보상 요약")
data class RewardsSummaryResponse(
    @field:Schema(description = "미수령 보상 묶음 수", example = "4")
    val totalPendingRewardCount: Int,
    @field:Schema(description = "미수령 에코잼 총합", example = "350")
    val pendingEcoJam: Int,
    @field:Schema(description = "미수령 포인트 총합", example = "0")
    val pendingPoint: Int,
    @field:Schema(description = "미수령 일반 재료 총합", example = "4")
    val pendingCommonIngredientCount: Int,
    @field:Schema(description = "미수령 히든 재료 총합", example = "1")
    val pendingHiddenIngredientCount: Int,
)

@Schema(description = "보상 묶음")
data class RewardBundleResponse(
    @field:Schema(description = "보상 묶음 ID", example = "101")
    val rewardId: Long,
    @field:Schema(description = "보상 출처 타입", example = "MISSION")
    val rewardSourceType: String,
    @field:Schema(description = "원본 리소스 ID", example = "55")
    val sourceId: Long,
    @field:Schema(description = "보상 출처 이름", example = "텀블러 사용 인증")
    val sourceTitle: String,
    @field:Schema(description = "보상 상태", example = "CLAIMABLE")
    val rewardStatus: String,
    @field:Schema(description = "보상 확정 시각", example = "2026-07-22T18:00:00")
    val earnedAt: String,
    @field:Schema(description = "보상 수령 시각", nullable = true)
    val claimedAt: String?,
    @field:Schema(description = "실제 지급 보상 목록")
    val rewards: List<RewardEntryResponse>,
)

@Schema(description = "보상 항목")
data class RewardEntryResponse(
    @field:Schema(description = "보상 타입", example = "INGREDIENT")
    val rewardType: String,
    @field:Schema(description = "재료 타입", example = "COMMON", nullable = true)
    val ingredientType: String? = null,
    @field:Schema(description = "재료 ID", example = "2", nullable = true)
    val ingredientId: Long? = null,
    @field:Schema(description = "재료 이름", example = "토마토", nullable = true)
    val ingredientName: String? = null,
    @field:Schema(description = "지급 수량", example = "1")
    val quantity: Int,
    @field:Schema(description = "재료 이미지 URL", nullable = true)
    val imageUrl: String? = null,
)

@Schema(description = "보상 개별 수령 응답")
data class ClaimRewardResponse(
    @field:Schema(description = "수령 완료한 보상 묶음 ID", example = "101")
    val rewardId: Long,
    @field:Schema(description = "보상 수령 시각", example = "2026-07-23T10:30:00")
    val claimedAt: String,
)

@Schema(description = "보상 일괄 수령 응답")
data class ClaimAllRewardsResponse(
    @field:Schema(description = "이번 요청으로 수령 완료한 보상 묶음 수", example = "4")
    val claimedRewardCount: Int,
    @field:Schema(description = "일괄 수령 처리 시각", example = "2026-07-23T10:31:00")
    val claimedAt: String,
)
