package com.zerost.api.communitymission.presentation.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "공동 미션 지급 재료 정보")
data class CommunityMissionRewardedIngredientResponse(

    @Schema(description = "재료 ID", example = "1")
    val ingredientId: Long,

    @Schema(description = "재료명", example = "토마토")
    val ingredientName: String,

    @Schema(description = "재료 타입", example = "COMMON")
    val ingredientType: String,

    @Schema(description = "지급 수량", example = "2")
    val quantity: Int,
)
