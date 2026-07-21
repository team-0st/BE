package com.zerost.api.mission.presentation.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

@Schema(description = "미션 인증 제출 요청")
data class SubmitMissionVerificationRequest(

    @field:NotBlank(message = "photoKey는 필수입니다.")
    @field:Size(max = 255, message = "photoKey는 255자 이하여야 합니다.")
    @field:Schema(
        description = "파일 업로드 API 응답으로 받은 미션 인증 이미지 파일 키",
        example = "missions/1/1/2026/07/18/550e8400-e29b-41d4-a716-446655440000.jpg",
    )
    val photoKey: String,
)
