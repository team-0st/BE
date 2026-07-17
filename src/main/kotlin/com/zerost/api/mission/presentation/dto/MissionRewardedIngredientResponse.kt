package com.zerost.api.mission.presentation.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "미션 보상 재료 정보")
data class MissionRewardedIngredientResponse(

    @Schema(description = "재료 식별자", example = "5")
    val id: Long,

    @Schema(description = "재료 이름", example = "낡은 밧줄")
    val name: String,

    @Schema(description = "재료 이미지 URL", example = "https://example.com/images/ingredient-5.png")
    val imageUrl: String?,
)
