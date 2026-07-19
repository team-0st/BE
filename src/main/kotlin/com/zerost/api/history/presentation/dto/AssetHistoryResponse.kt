package com.zerost.api.history.presentation.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "적립 내역 응답 항목")
data class AssetHistoryResponse(

    @Schema(description = "적립 내역 ID", example = "12")
    val historyId: Long,

    @Schema(description = "적립 금액", example = "300")
    val amount: Int,

    @Schema(description = "적립 발생 소스", example = "SOUP")
    val sourceType: String,

    @Schema(description = "적립 원천 데이터 ID", example = "101")
    val sourceId: Long,

    @Schema(description = "적립 시각", example = "2026-07-19T22:30:00")
    val createdAt: String,
)
