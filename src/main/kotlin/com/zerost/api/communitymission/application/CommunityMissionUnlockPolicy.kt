package com.zerost.api.communitymission.application

import com.zerost.api.communitymission.domain.CommunityMission

object CommunityMissionUnlockPolicy {

    fun sort(communityMissions: List<CommunityMission>): List<CommunityMission> {
        return communityMissions.sortedWith(
            compareBy<CommunityMission> { it.difficulty.order }
                .thenBy { it.stage }
                .thenBy { it.id },
        )
    }

    fun isUnlocked(
        communityMissions: List<CommunityMission>,
        communityMission: CommunityMission,
        completedMissionIds: Set<Long>,
    ): Boolean {
        if (communityMission.stage == 1) {
            return true
        }

        val previousMissionId = communityMissions
            .firstOrNull {
                it.difficulty == communityMission.difficulty && it.stage == communityMission.stage - 1
            }
            ?.id ?: return false

        return previousMissionId in completedMissionIds
    }
}
