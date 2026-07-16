package com.zerost.api.user.presentation

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

@Schema(description = "디바이스 등록 요청")
data class RegisterUserRequest(

    @field:NotBlank(message = "deviceId는 필수입니다.")
    @field:Size(max = 64, message = "deviceId는 64자 이하여야 합니다.")
    @Schema(
        description = "앱에서 발급하거나 저장한 디바이스 식별자",
        example = "550e8400-e29b-41d4-a716-446655440000"
    )
    val deviceId: String,
)
