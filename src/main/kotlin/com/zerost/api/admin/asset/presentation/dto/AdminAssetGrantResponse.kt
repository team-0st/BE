package com.zerost.api.admin.asset.presentation.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "관리자 자산 지급 결과")
data class AdminAssetGrantResponse(
    @Schema(description = "지급에 성공한 유저 수(중복 ID는 1명으로 집계)")
    val grantedUserCount: Int,
    @Schema(description = "실제 지급된 총량 (amount × userIds 건수)")
    val totalGrantedAmount: Int,
    @Schema(description = "유저별 지급 결과")
    val results: List<AdminAssetGrantItemResult>,
)

data class AdminAssetGrantItemResult(
    val userId: Long,
    val grantedAmount: Int,
    val balanceAfter: Int,
)
