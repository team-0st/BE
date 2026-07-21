package com.zerost.api.communitymission.presentation.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "공동 미션 인증 단계 응답")
data class CommunityMissionProofRequirementResponse(

    @Schema(description = "인증 단계 ID", example = "11")
    val requirementId: Long,

    @Schema(description = "인증 단계 순서", example = "1")
    val proofOrder: Int,

    @Schema(description = "인증 단계 제목", example = "1일차 인증", nullable = true)
    val title: String?,

    @Schema(description = "인증 단계 설명", example = "텀블러 사용 사진 제출", nullable = true)
    val description: String?,

    @Schema(description = "필수 이미지 수", example = "2")
    val requiredImageCount: Int,

    @Schema(description = "기준 일차 오프셋", example = "4", nullable = true)
    val requiredDayOffset: Int?,

    @Schema(description = "현재 유저 제출 여부", example = "true")
    val submitted: Boolean,

    @Schema(description = "제출 ID", example = "101", nullable = true)
    val submittedProofId: Long?,

    @Schema(description = "제출 시각", example = "2026-07-21T15:30:00", nullable = true)
    val submittedAt: String?,

    @Schema(description = "제출 이미지 파일 키 목록")
    val submittedImageKeys: List<String>,
)
