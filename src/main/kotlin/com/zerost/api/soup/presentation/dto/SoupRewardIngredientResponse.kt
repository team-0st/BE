package com.zerost.api.soup.presentation.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "스프 제작 보상 재료 응답")
data class SoupRewardIngredientResponse(
    @field:Schema(description = "지급된 재료 식별자", example = "1")
    val ingredientId: Long,

    @field:Schema(description = "지급된 재료 이름", example = "양배추")
    val ingredientName: String,

    @field:Schema(description = "지급된 수량", example = "1")
    val quantity: Int,
)
