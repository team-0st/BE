package com.zerost.api.user.presentation.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "온보딩 완료 응답")
data class CompleteOnboardingResponse(

    @Schema(description = "유저 식별자", example = "123")
    val userId: Long,

    @Schema(description = "유저 닉네임", example = "펭귄탐험가")
    val nickname: String,

    @Schema(description = "유저 전화번호", example = "010-1234-5678")
    val phoneNumber: String,

    @Schema(description = "선택한 상점 식별자", example = "1")
    val shopId: Long,
)
