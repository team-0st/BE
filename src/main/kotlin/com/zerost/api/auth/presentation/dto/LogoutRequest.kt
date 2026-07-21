package com.zerost.api.auth.presentation.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank

@Schema(description = "로그아웃 요청")
data class LogoutRequest(

    @field:NotBlank(message = "refresh token은 필수입니다.")
    @Schema(description = "폐기할 refresh token", example = "550e8400-e29b-41d4-a716-446655440000")
    val refreshToken: String,
)
