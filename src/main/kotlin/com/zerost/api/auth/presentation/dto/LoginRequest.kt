package com.zerost.api.auth.presentation.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

@Schema(description = "로그인 요청")
data class LoginRequest(

    @field:NotBlank(message = "휴대전화 번호는 필수입니다.")
    @field:Pattern(
        regexp = "^\\d{3}-\\d{4}-\\d{4}$",
        message = "휴대전화 번호는 010-1234-5678 형식이어야 합니다."
    )
    @Schema(description = "로그인 아이디로 사용하는 휴대전화 번호", example = "010-1234-5678")
    val phoneNumber: String,

    @field:NotBlank(message = "비밀번호는 필수입니다.")
    @field:Size(min = 8, max = 64, message = "비밀번호는 8자 이상 64자 이하여야 합니다.")
    @Schema(description = "로그인 비밀번호", example = "zerost1234")
    val password: String,
)
