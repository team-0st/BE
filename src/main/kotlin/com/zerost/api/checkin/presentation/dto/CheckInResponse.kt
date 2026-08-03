package com.zerost.api.checkin.presentation.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "출석 완료 응답")
data class CheckInResponse(

    @Schema(description = "랜덤으로 지급된 일반 재료 보상")
    val rewardedIngredient: RewardedIngredientResponse,
)
