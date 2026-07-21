package com.zerost.api.communitymission.application

import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

@Component
class CommunityMissionSucceededEventHandler(
    private val communityMissionRewardSettlementService: CommunityMissionRewardSettlementService,
) {

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun handle(event: CommunityMissionSucceededEvent) {
        communityMissionRewardSettlementService.settlePendingRewards(
            communityMissionId = event.communityMissionId,
            rewardedAt = event.rewardedAt,
        )
    }
}
