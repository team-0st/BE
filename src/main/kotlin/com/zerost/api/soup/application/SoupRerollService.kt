package com.zerost.api.soup.application

import com.zerost.api.common.exception.BusinessException
import com.zerost.api.common.exception.ErrorCode
import com.zerost.api.ecojam.domain.EcoJamHistory
import com.zerost.api.ecojam.domain.EcoJamHistoryRepository
import com.zerost.api.ecojam.domain.EcoJamHistorySourceType
import com.zerost.api.recipe.domain.RecipeType
import com.zerost.api.soup.domain.SoupRepository
import com.zerost.api.soup.domain.SoupRewardGrade
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
) {

    @Transactional
    fun reroll(deviceId: String, soupId: Long): RerollSoupResponse {
        val previewSoup = soupRepository.findById(soupId)
            .orElseThrow { BusinessException(ErrorCode.SOUP_NOT_FOUND) }
        if (previewSoup.user.deviceId != deviceId) {
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
        when (recipeType) {
            RecipeType.COMMON -> when (rewardGrade) {
                SoupRewardGrade.CONSOLATION -> 30
                SoupRewardGrade.INGREDIENT -> 50
                SoupRewardGrade.SMALL -> 70
                SoupRewardGrade.MIDDLE -> 100
                SoupRewardGrade.JACKPOT -> throw BusinessException(ErrorCode.SOUP_REROLL_NOT_AVAILABLE)
            }

            RecipeType.HIDDEN -> when (rewardGrade) {
                SoupRewardGrade.INGREDIENT -> 80
                SoupRewardGrade.SMALL -> 120
                SoupRewardGrade.MIDDLE -> 150
                SoupRewardGrade.JACKPOT,
                SoupRewardGrade.CONSOLATION,
                -> throw BusinessException(ErrorCode.SOUP_REROLL_NOT_AVAILABLE)
            }

            RecipeType.LEGENDARY -> when (rewardGrade) {
                SoupRewardGrade.INGREDIENT -> 100
                SoupRewardGrade.SMALL -> 150
                SoupRewardGrade.MIDDLE -> 200
                SoupRewardGrade.JACKPOT,
                SoupRewardGrade.CONSOLATION,
                -> throw BusinessException(ErrorCode.SOUP_REROLL_NOT_AVAILABLE)
            }
        }
}
