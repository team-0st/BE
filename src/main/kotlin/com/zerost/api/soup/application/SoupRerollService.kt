package com.zerost.api.soup.application

import com.zerost.api.common.exception.BusinessException
import com.zerost.api.common.exception.ErrorCode
import com.zerost.api.ecojam.domain.EcoJamHistory
import com.zerost.api.ecojam.domain.EcoJamHistoryRepository
import com.zerost.api.ecojam.domain.EcoJamHistorySourceType
import com.zerost.api.recipe.domain.RecipeType
import com.zerost.api.soup.domain.SoupRepository
import com.zerost.api.soup.domain.SoupRewardGrade
import com.zerost.api.soup.domain.SoupRerollPolicyGroupRepository
import com.zerost.api.soup.presentation.dto.RerollSoupResponse
import com.zerost.api.user.domain.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class SoupRerollService(
    private val userRepository: UserRepository,
    private val soupRepository: SoupRepository,
    private val soupRewardService: SoupRewardService,
    private val ecoJamHistoryRepository: EcoJamHistoryRepository,
    private val soupRerollPolicyGroupRepository: SoupRerollPolicyGroupRepository,
) {

    @Transactional
    fun reroll(userId: Long, soupId: Long): RerollSoupResponse {
        val previewSoup = soupRepository.findById(soupId)
            .orElseThrow { BusinessException(ErrorCode.SOUP_NOT_FOUND) }
        if (requireNotNull(previewSoup.user.id) != userId) {
            throw BusinessException(ErrorCode.SOUP_NOT_FOUND)
        }

        val user = userRepository.findByIdForUpdate(requireNotNull(previewSoup.user.id))
            .orElseThrow { BusinessException(ErrorCode.USER_NOT_FOUND) }
        val soup = soupRepository.findByIdForUpdate(soupId)
            .orElseThrow { BusinessException(ErrorCode.SOUP_NOT_FOUND) }

        if (soup.rerolled) {
            throw BusinessException(ErrorCode.SOUP_REROLL_ALREADY_COMPLETED)
        }

        val rerollCost = calculateRerollCost(soup.recipe.type, soup.rewardGrade)
        soupRewardService.validateRewardRecoverable(soup)
        if (user.ecoJam < rerollCost) {
            throw BusinessException(ErrorCode.INSUFFICIENT_ECO_JAM)
        }

        user.decreaseEcoJam(rerollCost)
        ecoJamHistoryRepository.save(
            EcoJamHistory.spend(
                user = user,
                amount = rerollCost,
                sourceType = EcoJamHistorySourceType.SOUP_REROLL,
                sourceId = requireNotNull(soup.id),
            ),
        )

        val reward = soupRewardService.reroll(soup)
        soup.markRerolled()

        return RerollSoupResponse(
            soupId = requireNotNull(soup.id),
            rerollCostEcoJam = rerollCost,
            remainingEcoJam = user.ecoJam,
            rewardGrade = reward.rewardGrade,
            rewardEcoJam = reward.ecoJam,
            rewardPoint = reward.point,
            rewardedIngredients = reward.rewardedIngredients,
        )
    }

    private fun calculateRerollCost(recipeType: RecipeType, rewardGrade: SoupRewardGrade): Int =
        soupRerollPolicyGroupRepository.findByRecipeTypeAndCurrentRewardGradeAndActiveTrue(
            recipeType = recipeType,
            currentRewardGrade = rewardGrade,
        ).map { it.rerollCostEcoJam }
            .orElseThrow { BusinessException(ErrorCode.SOUP_REROLL_NOT_AVAILABLE) }
}
