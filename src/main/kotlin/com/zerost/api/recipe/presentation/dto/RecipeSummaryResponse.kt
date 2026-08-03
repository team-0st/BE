package com.zerost.api.recipe.presentation.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "레시피 목록 응답 항목")
data class RecipeSummaryResponse(

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
)
