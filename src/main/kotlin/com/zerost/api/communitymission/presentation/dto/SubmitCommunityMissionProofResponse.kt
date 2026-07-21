package com.zerost.api.communitymission.presentation.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "공동 미션 인증 제출 응답")
data class SubmitCommunityMissionProofResponse(
    @Schema(description = "인증 제출 ID", example = "101")
    val proofId: Long,
    @Schema(description = "공동 미션 ID", example = "3")
    val communityMissionId: Long,
    @Schema(description = "인증 단계 ID", example = "11")
    val requirementId: Long,
    @Schema(description = "인증 단계 순서", example = "1")
    val proofOrder: Int,
    @Schema(description = "제출 시각", example = "2026-07-21T15:30:00")
    val submittedAt: String,
    @Schema(description = "완료 처리 가능 여부", example = "false")
    val readyToComplete: Boolean,
)
