package com.zerost.api.admin.asset.presentation.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull

@Schema(description = "관리자 자산 지급 요청")
data class AdminAssetGrantRequest(

    @field:NotEmpty
    @Schema(description = "지급 대상 유저 ID 목록 (중복 허용 — 중복 시 합산 지급)", example = "[1, 2, 2]")
    val userIds: List<Long>,

    @field:NotNull
    @Schema(description = "지급 자산 종류", example = "ECO_JAM", allowableValues = ["ECO_JAM", "POINT"])
    val assetType: AdminAssetType,

    @field:Min(1)
    @Schema(description = "1인당 지급 수량 (userIds에 같은 ID가 여러 번이면 그만큼 배수)", example = "500")
    val amount: Int,
)

enum class AdminAssetType {
    ECO_JAM,
    POINT,
}
