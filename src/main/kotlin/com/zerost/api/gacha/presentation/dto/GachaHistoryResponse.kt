package com.zerost.api.gacha.presentation.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "가챠 실행 내역 응답 항목")
data class GachaHistoryResponse(

    @Schema(description = "가챠 실행 ID", example = "21")
    val gachaId: Long,

    @Schema(description = "차감된 에코잼", example = "100")
    val costEcoJam: Int,

    @Schema(description = "가챠 결과 타입", example = "POINT")
    val resultType: String,

    @Schema(description = "획득한 포인트", example = "300")
    val resultPoint: Int,

    @Schema(description = "획득한 에코잼", example = "30")
    val resultEcoJam: Int,

    @Schema(description = "획득한 재료 ID", example = "5", nullable = true)
    val resultIngredientId: Long?,

    @Schema(description = "획득한 재료 이름", example = "양배추", nullable = true)
    val resultIngredientName: String?,

    @Schema(description = "획득한 재료 수량", example = "1")
    val resultIngredientQuantity: Int,

    @Schema(description = "가챠 실행 시각", example = "2026-07-19T14:30:00")
    val createdAt: String,
)
