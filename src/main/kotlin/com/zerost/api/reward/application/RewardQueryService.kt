package com.zerost.api.reward.application

import com.zerost.api.ingredient.domain.IngredientType
import com.zerost.api.mission.domain.MissionCompletion
import com.zerost.api.mission.domain.MissionCompletionRepository
import com.zerost.api.mission.domain.MissionCompletionStatus
import com.zerost.api.reward.presentation.dto.RewardBundleResponse
import com.zerost.api.reward.presentation.dto.RewardEntryResponse
import com.zerost.api.reward.presentation.dto.RewardsSummaryResponse
import com.zerost.api.reward.presentation.dto.RewardsTabResponse
import com.zerost.api.user.domain.UserRepository
import com.zerost.api.common.exception.BusinessException
import com.zerost.api.common.exception.ErrorCode
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class RewardQueryService(
    private val userRepository: UserRepository,
    private val missionCompletionRepository: MissionCompletionRepository,
) {

    @Transactional(readOnly = true)
    fun getRewards(userId: Long): RewardsTabResponse {
        if (!userRepository.existsById(userId)) {
            throw BusinessException(ErrorCode.USER_NOT_FOUND)
        }

        val claimableMissions = missionCompletionRepository
            .findAllByUserIdAndStatusAndRewardClaimedAtIsNullOrderByReviewedAtAscSubmittedAtAsc(
                userId,
                MissionCompletionStatus.APPROVED,
            )
            .filter { it.isRewardClaimable() }

        val items = claimableMissions.map { toMissionBundle(it) }
        return RewardsTabResponse(
            summary = buildSummary(items),
            items = items,
        )
    }

    fun listClaimableMissionCompletions(userId: Long): List<MissionCompletion> {
        return missionCompletionRepository
            .findAllByUserIdAndStatusAndRewardClaimedAtIsNullOrderByReviewedAtAscSubmittedAtAsc(
                userId,
                MissionCompletionStatus.APPROVED,
            )
            .filter { it.isRewardClaimable() }
    }

    private fun toMissionBundle(completion: MissionCompletion): RewardBundleResponse {
        val ingredient = requireNotNull(completion.rewardedIngredient)
        val earnedAt = (completion.reviewedAt ?: completion.submittedAt).toString()
        return RewardBundleResponse(
            rewardId = requireNotNull(completion.id),
            rewardSourceType = SOURCE_MISSION,
            sourceId = requireNotNull(completion.id),
            sourceTitle = completion.mission.title,
            rewardStatus = STATUS_CLAIMABLE,
            earnedAt = earnedAt,
            claimedAt = null,
            rewards = listOf(
                RewardEntryResponse(
                    rewardType = TYPE_INGREDIENT,
                    ingredientType = ingredient.type.name,
                    ingredientId = requireNotNull(ingredient.id),
                    ingredientName = ingredient.name,
                    quantity = 1,
                    imageUrl = ingredient.imageUrl,
                ),
            ),
        )
    }

    private fun buildSummary(items: List<RewardBundleResponse>): RewardsSummaryResponse {
        var pendingEcoJam = 0
        var pendingPoint = 0
        var pendingCommon = 0
        var pendingHidden = 0

        items.forEach { bundle ->
            bundle.rewards.forEach { entry ->
                when (entry.rewardType) {
                    TYPE_ECO_JAM -> pendingEcoJam += entry.quantity
                    TYPE_POINT -> pendingPoint += entry.quantity
                    TYPE_INGREDIENT -> when (entry.ingredientType) {
                        IngredientType.COMMON.name -> pendingCommon += entry.quantity
                        IngredientType.HIDDEN.name -> pendingHidden += entry.quantity
                    }
                }
            }
        }

        return RewardsSummaryResponse(
            totalPendingRewardCount = items.size,
            pendingEcoJam = pendingEcoJam,
            pendingPoint = pendingPoint,
            pendingCommonIngredientCount = pendingCommon,
            pendingHiddenIngredientCount = pendingHidden,
        )
    }

    companion object {
        const val SOURCE_MISSION = "MISSION"
        const val SOURCE_COMMUNITY_MISSION = "COMMUNITY_MISSION"
        const val STATUS_CLAIMABLE = "CLAIMABLE"
        const val TYPE_INGREDIENT = "INGREDIENT"
        const val TYPE_ECO_JAM = "ECO_JAM"
        const val TYPE_POINT = "POINT"
    }
}
