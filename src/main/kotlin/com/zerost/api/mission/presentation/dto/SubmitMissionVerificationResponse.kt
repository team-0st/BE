package com.zerost.api.mission.presentation.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "미션 인증 제출 응답")
data class SubmitMissionVerificationResponse(

    @Schema(description = "미션 인증 제출 ID", example = "55")
    val completionId: Long,

    @Schema(description = "미션 인증 검수 상태", example = "PENDING")
    val status: String,
)
