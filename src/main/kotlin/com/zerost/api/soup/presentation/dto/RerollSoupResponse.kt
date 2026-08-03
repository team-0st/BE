package com.zerost.api.soup.presentation.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "스프 리롤 응답")
data class RerollSoupResponse(

    @field:Schema(description = "스프 제작 이력 식별자", example = "10")
    val soupId: Long,

    @field:Schema(description = "리롤에 사용한 에코잼 비용", example = "30")
    val rerollCostEcoJam: Int,

    @field:Schema(description = "리롤 후 남은 에코잼", example = "70")
    val remainingEcoJam: Int,

    @field:Schema(description = "리롤 후 보상 등급", example = "SMALL")
    val rewardGrade: String,

    @field:Schema(description = "리롤 후 지급된 에코잼", example = "50")
    val rewardEcoJam: Int,

    @field:Schema(description = "리롤 후 지급된 포인트", example = "500")
    val rewardPoint: Int,

    val rewardedIngredients: List<SoupRewardIngredientResponse>,
)
