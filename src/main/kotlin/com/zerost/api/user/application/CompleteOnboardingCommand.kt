package com.zerost.api.user.application

data class CompleteOnboardingCommand(
    val deviceId: String,
    val nickname: String,
    val phoneNumber: String,
    val password: String,
    val shopId: Long,
)
