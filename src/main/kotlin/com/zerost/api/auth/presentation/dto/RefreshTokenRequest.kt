package com.zerost.api.auth.presentation.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank

@Schema(description = "토큰 재발급 요청")
data class RefreshTokenRequest(

    @field:NotBlank(message = "refresh token은 필수입니다.")
    @Schema(description = "재발급에 사용할 refresh token", example = "550e8400-e29b-41d4-a716-446655440000")
    val refreshToken: String,
)
