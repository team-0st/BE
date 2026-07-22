package com.zerost.api.recipe.presentation.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "레시피 섹션형 목록 응답")
data class RecipeSectionsResponse(

    @Schema(description = "입문 레시피 목록")
    val introRecipes: List<RecipeSummaryResponse>,

    @Schema(description = "보통 레시피 목록")
    val generalRecipes: List<RecipeSummaryResponse>,

    @Schema(description = "히든 레시피 목록")
    val hiddenRecipes: List<RecipeSummaryResponse>,

    @Schema(description = "전설의 레시피 목록")
    val legendaryRecipes: List<RecipeSummaryResponse>,
)
