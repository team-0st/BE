package com.zerost.api.profile.presentation.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "프로필 캐릭터 선택 응답")
data class UpdateProfileCharacterResponse(

    @Schema(description = "유저 식별자", example = "1")
    val userId: Long,

    @Schema(description = "선택된 프로필 캐릭터 코드", example = "BASIC_1")
    val profileCharacterCode: String,
)
