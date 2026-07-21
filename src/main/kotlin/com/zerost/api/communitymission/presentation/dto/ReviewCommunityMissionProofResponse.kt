package com.zerost.api.communitymission.presentation.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "공동 미션 인증 검수 응답")
data class ReviewCommunityMissionProofResponse(
    @Schema(description = "공동 미션 인증 제출 ID", example = "101")
    val proofId: Long,
    @Schema(description = "검수 상태", example = "APPROVED")
    val status: String,
    @Schema(description = "검수 시각", example = "2026-07-21T16:00:00")
    val reviewedAt: String,
)
