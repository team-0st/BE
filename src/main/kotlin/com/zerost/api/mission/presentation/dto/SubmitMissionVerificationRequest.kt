package com.zerost.api.mission.presentation.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

@Schema(description = "미션 인증 제출 요청")
data class SubmitMissionVerificationRequest(

    @field:NotBlank(message = "photoUrl은 필수입니다.")
    @field:Size(max = 255, message = "photoUrl은 255자 이하여야 합니다.")
    @Schema(
        description = "파일 업로드 API를 통해 업로드한 미션 인증 사진 URL",
        example = "https://zerost-s3-bucket.s3.ap-northeast-2.amazonaws.com/missions/2026/07/18/550e8400-e29b-41d4-a716-446655440000.jpg",
    )
    val photoUrl: String,
)
