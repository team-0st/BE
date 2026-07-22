package com.zerost.api.soup.presentation.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "스프 보상 구간 정보")
data class SoupRewardSectionResponse(

    @field:Schema(description = "보상 등급", example = "SMALL")
    val rewardGrade: String,

    @field:Schema(description = "지급된 에코잼", example = "50")
    val ecoJam: Int,

    @field:Schema(description = "지급된 포인트", example = "500")
    val point: Int,

    @field:Schema(description = "지급된 재료 목록")
    val rewardedIngredients: List<SoupRewardIngredientResponse>,
)
