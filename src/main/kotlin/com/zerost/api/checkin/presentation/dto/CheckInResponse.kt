package com.zerost.api.checkin.presentation.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "출석 완료 응답")
data class CheckInResponse(

    @Schema(description = "출석 보상 재료")
    val rewardedIngredient: RewardedIngredientResponse,
)
