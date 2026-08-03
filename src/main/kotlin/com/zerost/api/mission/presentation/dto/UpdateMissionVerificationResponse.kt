package com.zerost.api.mission.presentation.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "미션 인증 수정 응답")
data class UpdateMissionVerificationResponse(

    @field:Schema(description = "미션 제출 ID", example = "55")
    val completionId: Long,

    @field:Schema(description = "미션 ID", example = "1")
    val missionId: Long,

    @field:Schema(description = "수정 후 상태", example = "PENDING")
    val status: String,

    @field:Schema(
        description = "수정 후 인증 이미지 파일 키",
        example = "missions/1/1/2026/07/21/550e8400-e29b-41d4-a716-446655440000.jpg",
    )
    val photoKey: String,
)
