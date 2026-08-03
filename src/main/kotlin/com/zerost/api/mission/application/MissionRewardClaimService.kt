package com.zerost.api.mission.application

import com.zerost.api.common.exception.BusinessException
import com.zerost.api.common.exception.ErrorCode
import com.zerost.api.ingredient.domain.IngredientHistory
import com.zerost.api.ingredient.domain.IngredientHistoryRepository
import com.zerost.api.ingredient.domain.IngredientHistorySourceType
import com.zerost.api.ingredient.domain.UserIngredient
import com.zerost.api.ingredient.domain.UserIngredientRepository
import com.zerost.api.mission.domain.MissionCompletionRepository
import com.zerost.api.mission.presentation.dto.ClaimMissionRewardResponse
import com.zerost.api.mission.presentation.dto.MissionRewardedIngredientResponse
import com.zerost.api.user.domain.UserRepository
import java.time.LocalDateTime
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class MissionRewardClaimService(
    private val missionCompletionRepository: MissionCompletionRepository,
    private val userRepository: UserRepository,
    private val userIngredientRepository: UserIngredientRepository,
    private val ingredientHistoryRepository: IngredientHistoryRepository,
) {

    @Transactional
    fun claimReward(
        userId: Long,
        completionId: Long,
    ): ClaimMissionRewardResponse {
        val completion = missionCompletionRepository.findByIdForUpdate(completionId)
            .orElseThrow { BusinessException(ErrorCode.MISSION_COMPLETION_NOT_FOUND) }

        if (!completion.belongsTo(userId)) {
            throw BusinessException(ErrorCode.MISSION_COMPLETION_NOT_FOUND)
        }

        completion.validateClaimable()

        val lockedUser = userRepository.findByIdForUpdate(userId)
            .orElseThrow { BusinessException(ErrorCode.USER_NOT_FOUND) }
        val ingredient = completion.rewardedIngredient
            ?: throw BusinessException(ErrorCode.MISSION_REWARD_CLAIM_NOT_AVAILABLE)

        val userIngredient = userIngredientRepository.findByUserAndIngredient(lockedUser, ingredient)
            .orElseGet {
                UserIngredient(
                    user = lockedUser,
                    ingredient = ingredient,
                    quantity = 0,
                )
            }

        userIngredient.increaseQuantity()
        userIngredientRepository.save(userIngredient)

        ingredientHistoryRepository.save(
            IngredientHistory.earn(
                user = lockedUser,
                ingredient = ingredient,
                amount = 1,
                sourceType = IngredientHistorySourceType.MISSION,
                sourceId = requireNotNull(completion.id),
            ),
        )

        val claimedAt = LocalDateTime.now()
        completion.markRewardClaimed(claimedAt)
        log.info(
            "mission_reward_claimed userId={} completionId={} missionId={} ingredientId={} claimedAt={}",
            userId,
            completion.id,
            completion.mission.id,
            ingredient.id,
            claimedAt,
        )

        return ClaimMissionRewardResponse(
            completionId = requireNotNull(completion.id),
            missionId = requireNotNull(completion.mission.id),
            rewardedIngredient = MissionRewardedIngredientResponse(
                id = requireNotNull(ingredient.id),
                name = ingredient.name,
                imageUrl = ingredient.imageUrl,
            ),
            rewardClaimedAt = claimedAt.toString(),
        )
    }

    companion object {
        private val log = LoggerFactory.getLogger(MissionRewardClaimService::class.java)
    }
}
