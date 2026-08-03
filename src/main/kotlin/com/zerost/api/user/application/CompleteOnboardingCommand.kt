package com.zerost.api.user.application

data class CompleteOnboardingCommand(
    val userId: Long,
    val nickname: String,
    val phoneNumber: String,
    val password: String,
    val shopId: Long,
)
