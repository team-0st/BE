package com.zerost.api.admin.asset.presentation.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "관리자용 유저 요약")
data class AdminUserSummaryResponse(
    val userId: Long,
    val nickname: String?,
    val phoneNumber: String?,
    val onboardingCompleted: Boolean,
    val ecoJam: Int,
    val point: Int,
)
