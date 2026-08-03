package com.zerost.api.communitymission.application

import com.zerost.api.common.exception.BusinessException
import com.zerost.api.common.exception.ErrorCode
import com.zerost.api.communitymission.domain.CommunityMissionCompletion
import com.zerost.api.communitymission.domain.CommunityMissionCompletionRepository
import com.zerost.api.communitymission.domain.CommunityMissionReward
import com.zerost.api.communitymission.domain.CommunityMissionRewardType
import com.zerost.api.communitymission.presentation.dto.CommunityMissionRewardedIngredientResponse
import com.zerost.api.ecojam.domain.EcoJamHistory
import com.zerost.api.ecojam.domain.EcoJamHistoryRepository
import com.zerost.api.ecojam.domain.EcoJamHistorySourceType
import com.zerost.api.ingredient.domain.Ingredient
import com.zerost.api.ingredient.domain.IngredientHistory
import com.zerost.api.ingredient.domain.IngredientHistoryRepository
import com.zerost.api.ingredient.domain.IngredientHistorySourceType
import com.zerost.api.ingredient.domain.IngredientType
import com.zerost.api.ingredient.domain.UserIngredient
import com.zerost.api.ingredient.domain.UserIngredientRepository
import com.zerost.api.user.domain.User
import com.zerost.api.user.domain.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class CommunityMissionRewardSettlementBatchService(
    private val communityMissionCompletionRepository: CommunityMissionCompletionRepository,
    private val userIngredientRepository: UserIngredientRepository,
    private val ecoJamHistoryRepository: EcoJamHistoryRepository,
    private val ingredientHistoryRepository: IngredientHistoryRepository,
    private val userRepository: UserRepository,
    private val communityMissionRewardRandomProvider: CommunityMissionRewardRandomProvider,
) {

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun settleNextBatch(
        communityMissionId: Long,
        rewardedAt: LocalDateTime,
        rewards: List<CommunityMissionReward>,
        ingredientsByType: Map<IngredientType, List<Ingredient>>,
    ): Int {
        val pendingCompletions = communityMissionCompletionRepository
            .findTop100ByCommunityMissionIdAndRewardedAtIsNullOrderByIdAsc(communityMissionId)

        pendingCompletions.forEach { completion ->
            val lockedUser = userRepository.findByIdForUpdate(requireNotNull(completion.user.id))
                .orElseThrow { BusinessException(ErrorCode.USER_NOT_FOUND) }
            rewardCompletion(completion, lockedUser, rewardedAt, rewards, ingredientsByType)
        }

        return pendingCompletions.size
    }

    fun rewardCurrentCompletion(
        completion: CommunityMissionCompletion,
        user: User,
        rewardedAt: LocalDateTime,
        rewards: List<CommunityMissionReward>,
        ingredientsByType: Map<IngredientType, List<Ingredient>>,
    ): CommunityMissionRewardSettlementService.CompletionRewardResult {
        return rewardCompletion(completion, user, rewardedAt, rewards, ingredientsByType)
    }

    private fun rewardCompletion(
        completion: CommunityMissionCompletion,
        user: User,
        rewardedAt: LocalDateTime,
        rewards: List<CommunityMissionReward>,
        ingredientsByType: Map<IngredientType, List<Ingredient>>,
    ): CommunityMissionRewardSettlementService.CompletionRewardResult {
        if (completion.isRewarded()) {
            return CommunityMissionRewardSettlementService.CompletionRewardResult(
                rewardedEcoJam = 0,
                rewardedIngredients = emptyList(),
            )
        }

        var rewardedEcoJam = 0
        val rewardedIngredientCounts = linkedMapOf<Long, RewardedIngredientAccumulator>()
        val completionId = requireNotNull(completion.id)

        rewards.forEach { reward ->
            when (reward.rewardType) {
                CommunityMissionRewardType.ECO_JAM -> {
                    user.increaseEcoJam(reward.ecoJamAmount)
                    rewardedEcoJam += reward.ecoJamAmount
                    ecoJamHistoryRepository.save(
                        EcoJamHistory.earn(
                            user = user,
                            amount = reward.ecoJamAmount,
                            sourceType = EcoJamHistorySourceType.COMMUNITY_MISSION,
                            sourceId = completionId,
                        ),
                    )
                }

                CommunityMissionRewardType.INGREDIENT -> {
                    val ingredients = ingredientsByType.getValue(requireNotNull(reward.ingredientType))
                    repeat(reward.quantity) {
                        val ingredient = ingredients[communityMissionRewardRandomProvider.nextInt(ingredients.size)]
                        rewardIngredient(completionId, user, ingredient, rewardedIngredientCounts)
                    }
                }
            }
        }

        completion.markRewarded(rewardedAt)

        return CommunityMissionRewardSettlementService.CompletionRewardResult(
            rewardedEcoJam = rewardedEcoJam,
            rewardedIngredients = rewardedIngredientCounts.values.map { it.toResponse() },
        )
    }

    private fun rewardIngredient(
        completionId: Long,
        user: User,
        ingredient: Ingredient,
        rewardedIngredientCounts: MutableMap<Long, RewardedIngredientAccumulator>,
    ) {
        val userIngredient = userIngredientRepository.findByUserIdAndIngredientId(requireNotNull(user.id), requireNotNull(ingredient.id))
            ?: UserIngredient(
                user = user,
                ingredient = ingredient,
                quantity = 0,
            )
        userIngredient.increaseQuantity()
        userIngredientRepository.save(userIngredient)

        ingredientHistoryRepository.save(
            IngredientHistory.earn(
                user = userIngredient.user,
                ingredient = ingredient,
                amount = 1,
                sourceType = IngredientHistorySourceType.COMMUNITY_MISSION,
                sourceId = completionId,
            ),
        )

        val ingredientId = requireNotNull(ingredient.id)
        val accumulator = rewardedIngredientCounts.getOrPut(ingredientId) {
            RewardedIngredientAccumulator(
                ingredientId = ingredientId,
                ingredientName = ingredient.name,
                ingredientType = ingredient.type.name,
            )
        }
        accumulator.quantity += 1
    }

    private data class RewardedIngredientAccumulator(
        val ingredientId: Long,
        val ingredientName: String,
        val ingredientType: String,
        var quantity: Int = 0,
    ) {
        fun toResponse(): CommunityMissionRewardedIngredientResponse = CommunityMissionRewardedIngredientResponse(
            ingredientId = ingredientId,
            ingredientName = ingredientName,
            ingredientType = ingredientType,
            quantity = quantity,
        )
    }
}
