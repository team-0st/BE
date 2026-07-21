package com.zerost.api.mission.presentation.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "미션 인증 삭제 응답")
data class DeleteMissionVerificationResponse(

    @field:Schema(description = "삭제한 미션 제출 ID", example = "55")
    val completionId: Long,
)
