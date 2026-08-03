package com.zerost.api.user.presentation

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "임시 유저 등록 응답")
data class RegisterUserResponse(

    @Schema(description = "유저 식별자", example = "123")
    val userId: Long,

    @Schema(description = "온보딩 완료 여부", example = "false")
    val onboardingCompleted: Boolean,

    @Schema(description = "Access Token", example = "eyJhbGciOiJIUzI1NiJ9...")
    val accessToken: String,

    @Schema(description = "Refresh Token", example = "3d90cb08-b98d-4a3e-8c15-7569b6f51f78")
    val refreshToken: String,

    @Schema(description = "토큰 타입", example = "Bearer")
    val tokenType: String,

    @Schema(description = "Access Token 만료 시간(초)", example = "3600")
    val accessTokenExpiresIn: Long,

    @Schema(description = "Refresh Token 만료 시간(초)", example = "1209600")
    val refreshTokenExpiresIn: Long,
)
