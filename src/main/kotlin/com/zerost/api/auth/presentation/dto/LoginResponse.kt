package com.zerost.api.auth.presentation.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "로그인 응답")
data class LoginResponse(

    @Schema(description = "유저 식별자", example = "1")
    val userId: Long,

    @Schema(description = "유저 닉네임", example = "펭귄탐험가")
    val nickname: String,

    @Schema(description = "유저 휴대전화 번호", example = "010-1234-5678")
    val phoneNumber: String,

    @Schema(description = "온보딩 완료 여부", example = "true")
    val onboardingCompleted: Boolean,

    @Schema(description = "access token", example = "eyJhbGciOiJIUzI1NiJ9...")
    val accessToken: String,

    @Schema(description = "refresh token", example = "550e8400-e29b-41d4-a716-446655440000")
    val refreshToken: String,

    @Schema(description = "토큰 타입", example = "Bearer")
    val tokenType: String,

    @Schema(description = "access token 만료까지 남은 초", example = "3600")
    val accessTokenExpiresIn: Long,

    @Schema(description = "refresh token 만료까지 남은 초", example = "1209600")
    val refreshTokenExpiresIn: Long,
)
