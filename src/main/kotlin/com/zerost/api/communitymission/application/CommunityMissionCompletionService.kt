package com.zerost.api.communitymission.application

import com.zerost.api.common.exception.BusinessException
import com.zerost.api.common.exception.ErrorCode
import com.zerost.api.communitymission.domain.CommunityMissionCompletion
import com.zerost.api.communitymission.domain.CommunityMissionCompletionRepository
import com.zerost.api.communitymission.domain.CommunityMissionRepository
import com.zerost.api.communitymission.domain.CommunityMissionRewardRepository
import com.zerost.api.communitymission.domain.CommunityMissionRewardType
import com.zerost.api.ecojam.domain.EcoJamHistory
import com.zerost.api.ecojam.domain.EcoJamHistoryRepository
import com.zerost.api.ecojam.domain.EcoJamHistorySourceType
import com.zerost.api.ingredient.domain.Ingredient
import com.zerost.api.ingredient.domain.IngredientHistory
import com.zerost.api.ingredient.domain.IngredientHistoryRepository
import com.zerost.api.ingredient.domain.IngredientHistorySourceType
import com.zerost.api.ingredient.domain.IngredientRepository
import com.zerost.api.ingredient.domain.IngredientType
import com.zerost.api.ingredient.domain.UserIngredient
import com.zerost.api.ingredient.domain.UserIngredientRepository
import com.zerost.api.communitymission.presentation.dto.CompleteCommunityMissionResponse
import com.zerost.api.communitymission.presentation.dto.CommunityMissionRewardedIngredientResponse
import com.zerost.api.user.domain.User
import com.zerost.api.user.domain.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class CommunityMissionCompletionService(
    private val userRepository: UserRepository,
    private val communityMissionRepository: CommunityMissionRepository,
    private val communityMissionCompletionRepository: CommunityMissionCompletionRepository,
    private val communityMissionRewardRepository: CommunityMissionRewardRepository,
    private val ingredientRepository: IngredientRepository,
    private val userIngredientRepository: UserIngredientRepository,
    private val ecoJamHistoryRepository: EcoJamHistoryRepository,
    private val ingredientHistoryRepository: IngredientHistoryRepository,
    private val communityMissionRewardRandomProvider: CommunityMissionRewardRandomProvider,
) {

    @Transactional
    fun complete(userId: Long, communityMissionId: Long): CompleteCommunityMissionResponse {
        val user = userRepository.findByIdForUpdate(userId)
            .orElseThrow { BusinessException(ErrorCode.USER_NOT_FOUND) }

        if (!user.onboardingCompleted) {
            throw BusinessException(ErrorCode.COMMUNITY_MISSION_ONBOARDING_REQUIRED)
        }

        val communityMissions = CommunityMissionUnlockPolicy.sort(communityMissionRepository.findAllByActiveTrue())
        val communityMission = communityMissions.firstOrNull { it.id == communityMissionId }
            ?: throw BusinessException(ErrorCode.COMMUNITY_MISSION_NOT_FOUND)
        val resolvedUserId = requireNotNull(user.id)
        val completedMissionIds = communityMissionCompletionRepository.findCompletedMissionIdsByUserId(resolvedUserId).toSet()

        if (!CommunityMissionUnlockPolicy.isUnlocked(communityMissions, communityMission, completedMissionIds)) {
            throw BusinessException(ErrorCode.COMMUNITY_MISSION_NOT_UNLOCKED)
        }

        if (communityMissionCompletionRepository.existsByCommunityMissionIdAndUserId(communityMissionId, resolvedUserId)) {
            throw BusinessException(ErrorCode.COMMUNITY_MISSION_ALREADY_COMPLETED)
        }

        val completedAt = LocalDateTime.now()
        val completion = communityMissionCompletionRepository.save(
            CommunityMissionCompletion(
                communityMission = communityMission,
                user = user,
                completedAt = completedAt,
            ),
        )
        val completionId = requireNotNull(completion.id)
        val rewards = communityMissionRewardRepository.findAllByCommunityMissionIdOrderByRewardOrderAsc(communityMissionId)
        if (rewards.isEmpty()) {
            throw BusinessException(ErrorCode.COMMUNITY_MISSION_REWARD_NOT_FOUND)
        }

        var rewardedEcoJam = 0
        val rewardedIngredientCounts = linkedMapOf<Long, RewardedIngredientAccumulator>()

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
                    repeat(reward.quantity) {
                        val ingredient = pickRandomIngredient(requireNotNull(reward.ingredientType))
                        rewardIngredient(completionId, user, ingredient, rewardedIngredientCounts)
                    }
                }
            }
        }

        return CompleteCommunityMissionResponse(
            completionId = completionId,
            communityMissionId = requireNotNull(communityMission.id),
            rewardedEcoJam = rewardedEcoJam,
            rewardedIngredients = rewardedIngredientCounts.values.map { it.toResponse() },
            completedAt = completedAt.toString(),
        )
    }

    private fun pickRandomIngredient(type: IngredientType): Ingredient {
        val ingredients = ingredientRepository.findAllByType(type)
        if (ingredients.isEmpty()) {
            throw BusinessException(ErrorCode.INGREDIENT_NOT_FOUND)
        }
        return ingredients[communityMissionRewardRandomProvider.nextInt(ingredients.size)]
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
