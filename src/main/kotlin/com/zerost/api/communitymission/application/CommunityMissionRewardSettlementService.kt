package com.zerost.api.communitymission.application

import com.zerost.api.common.exception.BusinessException
import com.zerost.api.common.exception.ErrorCode
import com.zerost.api.communitymission.domain.CommunityMissionCompletionRepository
import com.zerost.api.communitymission.domain.CommunityMissionReward
import com.zerost.api.communitymission.domain.CommunityMissionRewardRepository
import com.zerost.api.communitymission.domain.CommunityMissionRewardType
import com.zerost.api.communitymission.presentation.dto.CommunityMissionRewardedIngredientResponse
import com.zerost.api.ingredient.domain.Ingredient
import com.zerost.api.ingredient.domain.IngredientRepository
import com.zerost.api.ingredient.domain.IngredientType
import com.zerost.api.user.domain.User
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class CommunityMissionRewardSettlementService(
    private val communityMissionCompletionRepository: CommunityMissionCompletionRepository,
    private val communityMissionRewardRepository: CommunityMissionRewardRepository,
    private val ingredientRepository: IngredientRepository,
    private val communityMissionRewardSettlementBatchService: CommunityMissionRewardSettlementBatchService,
) {

    fun settlePendingRewards(
        communityMissionId: Long,
        rewardedAt: LocalDateTime,
    ) {
        val rewards = loadRewards(communityMissionId)
        val ingredientsByType = loadIngredientsByType(rewards)

        while (true) {
            val processedCount = settleNextBatch(
                communityMissionId = communityMissionId,
                rewardedAt = rewardedAt,
                rewards = rewards,
                ingredientsByType = ingredientsByType,
            )

            if (processedCount == 0) {
                return
            }
        }
    }

    fun rewardCurrentCompletion(
        completion: com.zerost.api.communitymission.domain.CommunityMissionCompletion,
        user: User,
        rewardedAt: LocalDateTime,
    ): CompletionRewardResult {
        val rewards = loadRewards(requireNotNull(completion.communityMission.id))
        val ingredientsByType = loadIngredientsByType(rewards)
        return communityMissionRewardSettlementBatchService.rewardCurrentCompletion(
            completion = completion,
            user = user,
            rewardedAt = rewardedAt,
            rewards = rewards,
            ingredientsByType = ingredientsByType,
        )
    }

    fun settleNextBatch(
        communityMissionId: Long,
        rewardedAt: LocalDateTime,
        rewards: List<CommunityMissionReward>,
        ingredientsByType: Map<IngredientType, List<Ingredient>>,
    ): Int {
        return communityMissionRewardSettlementBatchService.settleNextBatch(
            communityMissionId = communityMissionId,
            rewardedAt = rewardedAt,
            rewards = rewards,
            ingredientsByType = ingredientsByType,
        )
    }

    private fun loadRewards(communityMissionId: Long): List<CommunityMissionReward> {
        val rewards = communityMissionRewardRepository.findAllByCommunityMissionIdOrderByRewardOrderAsc(communityMissionId)
        if (rewards.isEmpty()) {
            throw BusinessException(ErrorCode.COMMUNITY_MISSION_REWARD_NOT_FOUND)
        }
        return rewards
    }

    private fun loadIngredientsByType(
        rewards: List<CommunityMissionReward>,
    ): Map<IngredientType, List<Ingredient>> {
        return rewards
            .filter { it.rewardType == CommunityMissionRewardType.INGREDIENT }
            .mapNotNull { it.ingredientType }
            .distinct()
            .associateWith { type ->
                val ingredients = ingredientRepository.findAllByType(type)
                if (ingredients.isEmpty()) {
                    throw BusinessException(ErrorCode.INGREDIENT_NOT_FOUND)
                }
                ingredients
            }
    }

    data class CompletionRewardResult(
        val rewardedEcoJam: Int,
        val rewardedIngredients: List<CommunityMissionRewardedIngredientResponse>,
    )
}
