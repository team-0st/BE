package com.zerost.api.mission.presentation.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

@Schema(description = "미션 인증 제출 요청")
data class SubmitMissionVerificationRequest(

    @field:NotBlank(message = "photoUrl은 필수입니다.")
    @field:Size(max = 255, message = "photoUrl은 255자 이하여야 합니다.")
    @Schema(
        description = "업로드된 인증 사진 URL",
        example = "https://example.com/uploads/mission-1.jpg",
    )
    val photoUrl: String,
)
