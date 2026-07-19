package com.zerost.api.mission.presentation.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "관리자 미션 검수 응답")
data class ReviewMissionCompletionResponse(

    @field:Schema(description = "미션 인증 ID", example = "12")
    val completionId: Long,

    @field:Schema(description = "검수 결과 상태", example = "APPROVED")
    val status: String,

    @field:Schema(description = "검수 완료 시각", example = "2026-07-19T14:30:00")
    val reviewedAt: String,
)
