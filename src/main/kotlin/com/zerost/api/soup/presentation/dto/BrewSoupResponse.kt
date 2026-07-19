package com.zerost.api.soup.presentation.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "스프 제작 응답")
data class BrewSoupResponse(

    @field:Schema(description = "제작 이력 식별자", example = "10")
    val soupId: Long,

    @field:Schema(description = "제작된 레시피 식별자", example = "3")
    val recipeId: Long,

    @field:Schema(description = "제작된 스프 이름", example = "오리지널 스프")
    val recipeName: String,

    @field:Schema(description = "제작된 스프 타입", example = "COMMON")
    val recipeType: String,

    @field:Schema(description = "보상 등급", example = "JACKPOT")
    val rewardGrade: String,

    @field:Schema(description = "지급된 에코잼", example = "50")
    val rewardEcoJam: Int,

    @field:Schema(description = "지급된 포인트", example = "500")
    val rewardPoint: Int,

    val rewardedIngredients: List<SoupRewardIngredientResponse>,
)
