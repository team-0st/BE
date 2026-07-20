package com.zerost.api.home.presentation.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "오늘 미션 진행 현황")
data class HomeMissionProgressResponse(

    @Schema(description = "전체 미션 수", example = "7")
    val totalMissionCount: Int,

    @Schema(description = "오늘 제출한 미션 수", example = "2")
    val submittedMissionCount: Int,

    @Schema(description = "오늘 검수 대기 중인 미션 수", example = "1")
    val pendingMissionCount: Int,

    @Schema(description = "오늘 승인된 미션 수", example = "1")
    val approvedMissionCount: Int,

    @Schema(description = "오늘 반려된 미션 수", example = "0")
    val rejectedMissionCount: Int,
)
