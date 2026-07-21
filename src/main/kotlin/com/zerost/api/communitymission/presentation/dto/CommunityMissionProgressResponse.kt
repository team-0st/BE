package com.zerost.api.communitymission.presentation.dto

import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal

@Schema(description = "공동 미션 진행률 응답 항목")
data class CommunityMissionProgressResponse(

    @Schema(description = "공동 미션 ID", example = "1")
    val id: Long,

    @Schema(description = "공동 미션 제목", example = "오늘의 친환경 약속")
    val title: String,

    @Schema(description = "공동 미션 설명", example = "친구 또는 가족과 함께 오늘 실천할 친환경 행동 1가지를 정하고 함께 실천합니다.")
    val description: String?,

    @Schema(description = "공동 미션 이미지 URL", example = "https://example.com/images/community-mission-1.png")
    val imageUrl: String?,

    @Schema(description = "공동 미션 난이도", example = "ONE_STAR")
    val difficulty: String,

    @Schema(description = "동일 난이도 내 단계", example = "1")
    val stage: Int,

    @Schema(description = "목표 달성 비율(%)", example = "30.00")
    val targetRatio: BigDecimal,

    @Schema(description = "현재 달성 비율(%)", example = "42.86")
    val achievementRatio: BigDecimal,

    @Schema(description = "현재 완료 유저 수", example = "12")
    val participantCount: Long,

    @Schema(description = "기준 전체 유저 수", example = "28")
    val totalUserCount: Long,

    @Schema(description = "공동 미션 성공 여부", example = "true")
    val succeeded: Boolean,

    @Schema(description = "현재 유저 기준 해금 여부", example = "true")
    val unlocked: Boolean,

    @Schema(description = "현재 유저 완료 여부", example = "false")
    val completed: Boolean,
)
