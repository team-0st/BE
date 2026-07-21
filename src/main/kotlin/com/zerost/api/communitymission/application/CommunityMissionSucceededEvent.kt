package com.zerost.api.communitymission.application

import java.time.LocalDateTime

data class CommunityMissionSucceededEvent(
    val communityMissionId: Long,
    val rewardedAt: LocalDateTime,
)
