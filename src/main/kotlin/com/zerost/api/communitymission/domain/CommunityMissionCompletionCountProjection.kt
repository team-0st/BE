package com.zerost.api.communitymission.domain

interface CommunityMissionCompletionCountProjection {
    val communityMissionId: Long
    val completionCount: Long
}
