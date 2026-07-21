package com.zerost.api.profile.presentation.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "선택 가능한 프로필 캐릭터 정보")
data class ProfileCharacterResponse(

    @Schema(description = "프로필 캐릭터 코드", example = "BASIC_1")
    val code: String,

    @Schema(description = "프로필 캐릭터 이름", example = "기본 캐릭터 1")
    val name: String,

    @Schema(description = "프로필 캐릭터 설명", example = "선택형 프로필 기본 캐릭터 1")
    val description: String,
)
