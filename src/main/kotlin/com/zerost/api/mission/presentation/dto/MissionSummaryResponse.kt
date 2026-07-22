package com.zerost.api.mission.presentation.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "미션 목록 응답 항목")
data class MissionSummaryResponse(

    @Schema(description = "미션 ID", example = "1")
    val id: Long,

    @Schema(description = "미션 제목", example = "텀블러 사용하기")
    val title: String,

    @Schema(description = "미션 설명", example = "개인 컵 또는 텀블러를 사용한 사진을 제출합니다.")
    val description: String?,

    @Schema(description = "미션 이미지 URL", example = "https://example.com/images/mission-1.png")
    val imageUrl: String?,

    @Schema(
        description = "오늘 제출 상태. 오늘 제출 이력이 없으면 null",
        example = "PENDING",
        nullable = true,
    )
    val todayStatus: MissionTodayStatus?,

    @Schema(description = "오늘 승인된 미션 보상 수령 가능 여부", example = "false")
    val rewardClaimable: Boolean = false,

    @Schema(description = "오늘 승인된 미션 보상 수령 여부", example = "false")
    val rewardClaimed: Boolean = false,
)
