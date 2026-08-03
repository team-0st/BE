package com.zerost.api.user.presentation.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

@Schema(description = "닉네임 변경 요청")
data class UpdateNicknameRequest(

    @field:NotBlank(message = "닉네임은 필수입니다.")
    @field:Size(max = 50, message = "닉네임은 50자 이하여야 합니다.")
    @field:Schema(description = "변경할 닉네임", example = "펭귄탐험가")
    val nickname: String,
)
