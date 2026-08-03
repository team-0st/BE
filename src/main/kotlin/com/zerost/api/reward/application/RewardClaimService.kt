package com.zerost.api.reward.application

import com.zerost.api.common.exception.BusinessException
import com.zerost.api.common.exception.ErrorCode
import com.zerost.api.mission.application.MissionRewardClaimService
import com.zerost.api.reward.presentation.dto.ClaimAllRewardsResponse
import com.zerost.api.reward.presentation.dto.ClaimRewardResponse
import java.time.LocalDateTime
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * v1: MISSION 출처만 지원. rewardId = mission_completions.id
 * COMMUNITY_MISSION은 응답 구조만 확장 가능하도록 두고, 현재 자동 정산이라 미수령 목록에 포함하지 않는다.
 */
@Service
class RewardClaimService(
    private val rewardQueryService: RewardQueryService,
    private val missionRewardClaimService: MissionRewardClaimService,
) {

    @Transactional
    fun claimReward(userId: Long, rewardId: Long): ClaimRewardResponse {
        try {
            val claimed = missionRewardClaimService.claimReward(userId, rewardId)
            return ClaimRewardResponse(
                rewardId = rewardId,
                claimedAt = claimed.rewardClaimedAt,
            )
        } catch (error: BusinessException) {
            throw mapMissionClaimError(error)
        }
    }

    @Transactional
    fun claimAll(userId: Long): ClaimAllRewardsResponse {
        val claimable = rewardQueryService.listClaimableMissionCompletions(userId)
        var claimedCount = 0
        var lastClaimedAt: String? = null

        claimable.forEach { completion ->
            val response = claimReward(userId, requireNotNull(completion.id))
            claimedCount += 1
            lastClaimedAt = response.claimedAt
        }

        return ClaimAllRewardsResponse(
            claimedRewardCount = claimedCount,
            claimedAt = lastClaimedAt ?: LocalDateTime.now().toString(),
        )
    }

    private fun mapMissionClaimError(error: BusinessException): BusinessException {
        return when (error.errorCode) {
            ErrorCode.MISSION_COMPLETION_NOT_FOUND -> BusinessException(ErrorCode.REWARD_NOT_FOUND)
            ErrorCode.MISSION_REWARD_CLAIM_NOT_AVAILABLE -> BusinessException(ErrorCode.REWARD_NOT_CLAIMABLE)
            ErrorCode.MISSION_REWARD_ALREADY_CLAIMED -> BusinessException(ErrorCode.REWARD_ALREADY_CLAIMED)
            else -> error
        }
    }
}
