package com.zerost.api.profile.presentation.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank

@Schema(description = "프로필 캐릭터 선택 요청")
data class UpdateProfileCharacterRequest(

    @field:NotBlank(message = "profileCharacterCode는 필수입니다.")
    @Schema(description = "선택할 프로필 캐릭터 코드", example = "BASIC_1")
    val profileCharacterCode: String,
)
