package com.zerost.api.communitymission.application

import com.zerost.api.communitymission.domain.CommunityMissionCompletionRepository
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class CommunityMissionRewardRecoveryScheduler(
    private val communityMissionCompletionRepository: CommunityMissionCompletionRepository,
    private val communityMissionRewardSettlementService: CommunityMissionRewardSettlementService,
) {

    @Scheduled(fixedDelay = 60000, initialDelay = 60000)
    fun recoverPendingRewards() {
        val missionIds = communityMissionCompletionRepository.findDistinctSucceededCommunityMissionIdsWithPendingRewards()

        missionIds.forEach { communityMissionId ->
            communityMissionRewardSettlementService.settlePendingRewards(
                communityMissionId = communityMissionId,
                rewardedAt = LocalDateTime.now(),
            )
        }
    }
}
