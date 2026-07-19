package com.zerost.api.soup.presentation.dto

import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.Size

@Schema(description = "스프 제작 요청")
data class BrewSoupRequest(

    @field:NotEmpty(message = "ingredientIds는 비어 있을 수 없습니다.")
    @field:Size(min = 3, max = 5, message = "ingredientIds는 3개 이상 5개 이하여야 합니다.")
    @field:ArraySchema(
        schema = Schema(description = "선택한 재료 식별자", example = "1"),
        arraySchema = Schema(
            description = "선택한 재료 식별자 목록. 재료 순서도 레시피 판별에 사용됩니다.",
            example = "[1, 2, 3]",
        ),
    )
    val ingredientIds: List<Long>,
)
