package com.zerost.api.mission.presentation.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank

@Schema(description = "관리자 미션 검수 요청")
data class ReviewMissionCompletionRequest(

    @field:NotBlank(message = "상태는 필수입니다.")
    @field:Schema(
        description = "검수 결과 상태. APPROVED 또는 REJECTED만 허용합니다.",
        example = "APPROVED",
        allowableValues = ["APPROVED", "REJECTED"],
    )
    val status: String,
)
