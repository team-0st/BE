package com.zerost.api.mission.application

import com.zerost.api.common.exception.BusinessException
import com.zerost.api.common.exception.ErrorCode
import com.zerost.api.ingredient.domain.IngredientRepository
import com.zerost.api.mission.domain.MissionCompletion
import com.zerost.api.mission.domain.MissionCompletionRepository
import com.zerost.api.mission.presentation.dto.ReviewMissionCompletionResponse
import java.time.LocalDateTime
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AdminMissionReviewService(
    private val missionCompletionRepository: MissionCompletionRepository,
    private val ingredientRepository: IngredientRepository,
) {

    @Transactional
    fun reviewMissionCompletion(
        completionId: Long,
        status: String,
    ): ReviewMissionCompletionResponse {
        val completion = missionCompletionRepository.findByIdForUpdate(completionId)
            .orElseThrow { BusinessException(ErrorCode.MISSION_COMPLETION_NOT_FOUND) }

        val reviewedAt = LocalDateTime.now()

        when (status) {
            "APPROVED" -> {
                completion.approve(reviewedAt)
                assignRewardIngredient(completion)
            }
            "REJECTED" -> completion.reject(reviewedAt)
            else -> throw BusinessException(ErrorCode.INVALID_INPUT_VALUE)
        }

        return ReviewMissionCompletionResponse(
            completionId = requireNotNull(completion.id),
            status = completion.status.name,
            reviewedAt = requireNotNull(completion.reviewedAt).toString(),
        )
    }

    private fun assignRewardIngredient(completion: MissionCompletion) {
        val rewardIngredientId = completion.mission.pickRewardIngredientId()

        val ingredient = ingredientRepository.findById(rewardIngredientId)
            .orElseThrow { BusinessException(ErrorCode.INGREDIENT_NOT_FOUND) }
        completion.assignRewardedIngredient(ingredient)
    }
}
