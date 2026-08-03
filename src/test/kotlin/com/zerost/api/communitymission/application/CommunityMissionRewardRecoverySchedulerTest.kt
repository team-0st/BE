package com.zerost.api.communitymission.application

import com.zerost.api.communitymission.domain.CommunityMissionCompletionRepository
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor
import org.mockito.Mockito.any
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`

@Suppress("UNCHECKED_CAST")
private fun <T> schedulerAnyObject(): T {
    any<T>()
    return null as T
}

class CommunityMissionRewardRecoverySchedulerTest {

    private val communityMissionCompletionRepository = mock(CommunityMissionCompletionRepository::class.java)
    private val communityMissionRewardSettlementService = mock(CommunityMissionRewardSettlementService::class.java)
    private val communityMissionRewardRecoveryScheduler = CommunityMissionRewardRecoveryScheduler(
        communityMissionCompletionRepository = communityMissionCompletionRepository,
        communityMissionRewardSettlementService = communityMissionRewardSettlementService,
    )

    @Test
    fun `복구 스케줄러는 미지급 보상이 남은 성공 공동미션을 다시 정산한다`() {
        `when`(communityMissionCompletionRepository.findDistinctSucceededCommunityMissionIdsWithPendingRewards())
            .thenReturn(listOf(1L, 2L))

        communityMissionRewardRecoveryScheduler.recoverPendingRewards()

        val missionIdCaptor = ArgumentCaptor.forClass(Long::class.javaObjectType)
        verify(communityMissionRewardSettlementService, times(2)).settlePendingRewards(
            missionIdCaptor.capture(),
            schedulerAnyObject(),
        )
        kotlin.test.assertEquals(listOf(1L, 2L), missionIdCaptor.allValues)
    }
}
