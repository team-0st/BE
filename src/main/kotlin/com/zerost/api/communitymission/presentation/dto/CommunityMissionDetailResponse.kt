package com.zerost.api.communitymission.presentation.dto

import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal

@Schema(description = "공동 미션 상세 응답")
data class CommunityMissionDetailResponse(
    @Schema(description = "공동 미션 ID", example = "1")
    val id: Long,
    @Schema(description = "공동 미션 제목", example = "오늘의 친환경 약속")
    val title: String,
    @Schema(description = "공동 미션 설명", nullable = true)
    val description: String?,
    @Schema(description = "공동 미션 이미지 URL", nullable = true)
    val imageUrl: String?,
    @Schema(description = "공동 미션 난이도", example = "ONE_STAR")
    val difficulty: String,
    @Schema(description = "동일 난이도 내 단계", example = "1")
    val stage: Int,
    @Schema(description = "목표 달성 비율(%)", example = "30.00")
    val targetRatio: BigDecimal,
    @Schema(description = "공동 미션 성공 여부", example = "false")
    val succeeded: Boolean,
    @Schema(description = "현재 유저 기준 해금 여부", example = "true")
    val unlocked: Boolean,
    @Schema(description = "현재 유저 완료 여부", example = "false")
    val completed: Boolean,
    @Schema(description = "필수 인증 단계 수", example = "3")
    val requiredProofCount: Int,
    @Schema(description = "현재 유저 제출 완료 단계 수", example = "1")
    val submittedProofCount: Int,
    @Schema(description = "완료 처리 가능 여부", example = "false")
    val readyToComplete: Boolean,
    @Schema(description = "인증 단계 목록")
    val proofRequirements: List<CommunityMissionProofRequirementResponse>,
)
