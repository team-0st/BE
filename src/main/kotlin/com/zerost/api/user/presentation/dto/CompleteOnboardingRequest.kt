package com.zerost.api.user.presentation.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

@Schema(description = "온보딩 완료 요청")
data class CompleteOnboardingRequest(

    @field:NotBlank(message = "닉네임은 필수입니다.")
    @field:Size(max = 50, message = "닉네임은 50자 이하여야 합니다.")
    @Schema(description = "닉네임", example = "펭귄탐험가")
    val nickname: String,

    @field:NotBlank(message = "전화번호는 필수입니다.")
    @field:Size(max = 20, message = "전화번호는 20자 이하여야 합니다.")
    @field:Pattern(
        regexp = "^\\d{3}-\\d{4}-\\d{4}$",
        message = "전화번호는 010-1234-5678 형식이어야 합니다."
    )
    @Schema(description = "전화번호", example = "010-1234-5678")
    val phoneNumber: String,

    @field:NotBlank(message = "비밀번호는 필수입니다.")
    @field:Size(min = 8, max = 64, message = "비밀번호는 8자 이상 64자 이하여야 합니다.")
    @Schema(description = "로그인 비밀번호", example = "zerost1234")
    val password: String,

    @field:NotNull(message = "상점 ID는 필수입니다.")
    @Schema(description = "선택한 상점 식별자", example = "1")
    val shopId: Long?,
)
