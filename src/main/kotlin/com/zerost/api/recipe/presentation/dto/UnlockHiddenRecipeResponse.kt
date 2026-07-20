package com.zerost.api.recipe.presentation.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "희귀 레시피 랜덤 해금 응답")
data class UnlockHiddenRecipeResponse(

    @Schema(description = "해금된 레시피 ID", example = "2")
    val recipeId: Long,

    @Schema(description = "해금된 레시피 이름", example = "크리스탈 스프")
    val recipeName: String,

    @Schema(description = "해금 후 남은 에코잼", example = "300")
    val remainingEcoJam: Int,
)
