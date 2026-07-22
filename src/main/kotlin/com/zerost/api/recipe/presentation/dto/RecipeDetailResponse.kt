package com.zerost.api.recipe.presentation.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "레시피 상세 조회 응답")
data class RecipeDetailResponse(

    @Schema(description = "레시피 ID", example = "1")
    val recipeId: Long,

    @Schema(description = "레시피 이름. 비공개 레시피는 ??? 로 마스킹됩니다.", example = "오리지널 스프")
    val name: String,

    @Schema(description = "레시피 타입", example = "COMMON")
    val type: String,

    @Schema(description = "필요 슬롯 수", example = "3")
    val slotCount: Int,

    @Schema(description = "재료 조합 공개 여부", example = "true")
    val recipeVisible: Boolean,

    @Schema(description = "레시피 힌트 목록")
    val hints: List<RecipeHintResponse>,

    @Schema(description = "공개 가능한 경우에만 반환되는 재료 목록")
    val ingredients: List<RecipeDetailIngredientResponse>,
)
