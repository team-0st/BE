package com.zerost.api.recipe.presentation.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "레시피 상세의 재료 항목")
data class RecipeDetailIngredientResponse(

    @Schema(description = "재료 ID", example = "1")
    val ingredientId: Long,

    @Schema(description = "재료 이름", example = "양배추")
    val name: String,

    @Schema(description = "재료 타입", example = "COMMON")
    val type: String,

    @Schema(description = "재료 이미지 URL", example = "https://example.com/images/ingredient-1.png")
    val imageUrl: String?,

    @Schema(description = "슬롯 순서", example = "1")
    val slotOrder: Int,
)
