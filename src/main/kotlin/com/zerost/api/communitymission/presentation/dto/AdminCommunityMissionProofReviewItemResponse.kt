package com.zerost.api.communitymission.presentation.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "관리자 공동 미션 검수 대기 항목")
data class AdminCommunityMissionProofReviewItemResponse(
    @Schema(description = "공동 미션 인증 제출 ID", example = "101")
    val proofId: Long,
    @Schema(description = "공동 미션 ID", example = "3")
    val communityMissionId: Long,
    @Schema(description = "공동 미션 제목", example = "7일 일회용컵 없이 생활하기")
    val communityMissionTitle: String,
    @Schema(description = "인증 단계 ID", example = "11")
    val requirementId: Long,
    @Schema(description = "인증 단계 순서", example = "1")
    val proofOrder: Int,
    @Schema(description = "인증 단계 제목", example = "1일차 인증", nullable = true)
    val requirementTitle: String?,
    @Schema(description = "제출 유저 ID", example = "1")
    val userId: Long,
    @Schema(description = "제출 유저 닉네임", example = "펭귄탐험가", nullable = true)
    val nickname: String?,
    @Schema(description = "제출 시각", example = "2026-07-21T15:30:00")
    val submittedAt: String,
    @Schema(description = "제출 이미지 파일 키 목록")
    val imageKeys: List<String>,
)
