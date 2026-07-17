package com.zerost.api.ingredient.presentation.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "보유 재료 응답 항목")
data class UserIngredientResponse(

    @Schema(description = "재료 ID", example = "1")
    val ingredientId: Long,

    @Schema(description = "재료 이름", example = "버려진 천")
    val name: String,

    @Schema(description = "재료 타입", example = "COMMON")
    val type: String,

    @Schema(description = "재료 이미지 URL", example = "https://example.com/images/ingredient-1.png")
    val imageUrl: String?,

    @Schema(description = "보유 수량", example = "3")
    val quantity: Int,
)
