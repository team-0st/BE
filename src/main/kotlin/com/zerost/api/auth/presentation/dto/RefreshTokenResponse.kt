package com.zerost.api.auth.presentation.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "토큰 재발급 응답")
data class RefreshTokenResponse(

    @Schema(description = "새 access token", example = "eyJhbGciOiJIUzI1NiJ9...")
    val accessToken: String,

    @Schema(description = "새 refresh token", example = "550e8400-e29b-41d4-a716-446655440000")
    val refreshToken: String,

    @Schema(description = "토큰 타입", example = "Bearer")
    val tokenType: String,

    @Schema(description = "access token 만료까지 남은 초", example = "3600")
    val accessTokenExpiresIn: Long,

    @Schema(description = "refresh token 만료까지 남은 초", example = "1209600")
    val refreshTokenExpiresIn: Long,
)
