package com.zerost.api.user.presentation

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "디바이스 등록 응답")
data class RegisterUserResponse(

    @Schema(description = "유저 식별자", example = "123")
    val userId: Long,

    @Schema(description = "온보딩 완료 여부", example = "false")
    val onboardingCompleted: Boolean,
)
