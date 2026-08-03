package com.zerost.api.profile.presentation.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "선택 가능한 프로필 캐릭터 정보")
data class ProfileCharacterResponse(

    @Schema(description = "프로필 캐릭터 코드", example = "BROCCOLI")
    val code: String,

    @Schema(description = "프로필 캐릭터 이름", example = "브로콜리")
    val name: String,

    @Schema(description = "프로필 캐릭터 설명", example = "브로콜리 프로필 캐릭터")
    val description: String,

    @Schema(
        description = "프로필 캐릭터 공용 이미지 URL",
        example = "https://assets.zero-st.com/profile-characters/broccoli.png",
    )
    val imageUrl: String,
)
