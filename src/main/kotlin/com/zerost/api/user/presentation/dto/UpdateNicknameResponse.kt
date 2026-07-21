package com.zerost.api.user.presentation.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "닉네임 변경 응답")
data class UpdateNicknameResponse(

    @field:Schema(description = "유저 식별자", example = "1")
    val userId: Long,

    @field:Schema(description = "변경된 닉네임", example = "펭귄탐험가")
    val nickname: String,
)
