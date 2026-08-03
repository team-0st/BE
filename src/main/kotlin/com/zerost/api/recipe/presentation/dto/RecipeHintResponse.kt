package com.zerost.api.recipe.presentation.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "레시피 힌트")
data class RecipeHintResponse(

    @Schema(description = "힌트 난이도", example = "EASY")
    val level: String,

    @Schema(description = "힌트 내용", example = "붉은 채소와 향긋한 채소를 먼저 떠올려 보세요.")
    val content: String,
)
