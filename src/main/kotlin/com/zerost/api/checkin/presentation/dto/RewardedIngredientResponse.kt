package com.zerost.api.checkin.presentation.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "출석 보상 재료 정보")
data class RewardedIngredientResponse(

    @Schema(description = "재료 ID", example = "1")
    val id: Long,

    @Schema(description = "재료 이름", example = "버려진 천")
    val name: String,

    @Schema(description = "지급된 보상 재료 타입", example = "COMMON")
    val type: String,

    @Schema(description = "재료 이미지 URL", example = "https://example.com/images/ingredient-1.png")
    val imageUrl: String?,
)
