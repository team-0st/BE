package com.zerost.api.communitymission.presentation.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank

@Schema(description = "공동 미션 인증 검수 요청")
data class ReviewCommunityMissionProofRequest(
    @field:NotBlank(message = "status는 필수입니다.")
    @Schema(description = "검수 상태", example = "APPROVED", allowableValues = ["APPROVED", "REJECTED"])
    val status: String,
)
