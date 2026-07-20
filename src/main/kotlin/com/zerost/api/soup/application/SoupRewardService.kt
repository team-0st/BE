package com.zerost.api.soup.application

import com.zerost.api.common.exception.BusinessException
import com.zerost.api.common.exception.ErrorCode
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
import com.zerost.api.point.domain.PointHistory
import com.zerost.api.point.domain.PointHistoryRepository
import com.zerost.api.point.domain.PointHistorySourceType
import com.zerost.api.recipe.domain.RecipeType
import com.zerost.api.soup.domain.Soup
import com.zerost.api.soup.domain.SoupRewardGrade
import com.zerost.api.soup.domain.SoupRewardIngredient
import com.zerost.api.soup.domain.SoupRewardIngredientRepository
import com.zerost.api.soup.presentation.dto.SoupRewardIngredientResponse
import com.zerost.api.soup.presentation.dto.SoupRewardSummary
import org.springframework.stereotype.Service

@Service
class SoupRewardService(
    private val ingredientRepository: IngredientRepository,
    private val userIngredientRepository: UserIngredientRepository,
    private val soupRewardIngredientRepository: SoupRewardIngredientRepository,
    private val ingredientHistoryRepository: IngredientHistoryRepository,
    private val ecoJamHistoryRepository: EcoJamHistoryRepository,
    private val pointHistoryRepository: PointHistoryRepository,
    private val randomProvider: RandomProvider,
) {

    fun reward(soup: Soup): SoupRewardSummary {
        val reward = when (soup.recipe.type) {
            RecipeType.COMMON -> rewardCommonSoup()
            RecipeType.HIDDEN -> rewardHiddenSoup()
            RecipeType.LEGENDARY -> rewardLegendarySoup()
        }

        applyReward(soup, reward)

        return SoupRewardSummary(
            rewardGrade = reward.rewardGrade.name,
            ecoJam = reward.ecoJam,
            point = reward.point,
            rewardedIngredients = reward.rewardedIngredients.map { ingredientReward ->
                SoupRewardIngredientResponse(
                    ingredientId = requireNotNull(ingredientReward.ingredient.id),
                    ingredientName = ingredientReward.ingredient.name,
                    quantity = ingredientReward.quantity,
                )
            },
        )
    }

    fun reroll(soup: Soup): SoupRewardSummary {
        revokeReward(soup)

        val reward = when (soup.recipe.type) {
            RecipeType.COMMON -> rerollCommonSoup(soup.rewardGrade)
            RecipeType.HIDDEN -> rerollHiddenSoup(soup.rewardGrade)
            RecipeType.LEGENDARY -> rerollLegendarySoup(soup.rewardGrade)
        }

        applyReward(
            soup = soup,
            reward = reward,
            ecoJamHistorySourceType = EcoJamHistorySourceType.SOUP_REROLL,
            pointHistorySourceType = PointHistorySourceType.SOUP_REROLL,
            ingredientHistorySourceType = IngredientHistorySourceType.SOUP_REROLL,
        )

        return SoupRewardSummary(
            rewardGrade = reward.rewardGrade.name,
            ecoJam = reward.ecoJam,
            point = reward.point,
            rewardedIngredients = reward.rewardedIngredients.map { ingredientReward ->
                SoupRewardIngredientResponse(
                    ingredientId = requireNotNull(ingredientReward.ingredient.id),
                    ingredientName = ingredientReward.ingredient.name,
                    quantity = ingredientReward.quantity,
                )
            },
        )
    }

    fun validateRewardRecoverable(soup: Soup) {
        if (soup.user.ecoJam < soup.rewardEcoJam || soup.user.point < soup.rewardPoint) {
            throw BusinessException(ErrorCode.SOUP_REROLL_REWARD_RECOVERY_NOT_AVAILABLE)
        }

        val rewardedIngredients = soupRewardIngredientRepository.findAllBySoupIdOrderByIdAsc(requireNotNull(soup.id))
        rewardedIngredients.forEach { rewardedIngredient ->
            val userIngredient = userIngredientRepository.findByUserAndIngredient(soup.user, rewardedIngredient.ingredient)
                .orElseThrow { BusinessException(ErrorCode.SOUP_REROLL_REWARD_RECOVERY_NOT_AVAILABLE) }

            if (userIngredient.quantity < rewardedIngredient.quantity) {
                throw BusinessException(ErrorCode.SOUP_REROLL_REWARD_RECOVERY_NOT_AVAILABLE)
            }
        }
    }

    private fun rewardCommonSoup(): RewardResult {
        val roll = randomProvider.nextInt(100)

        return when {
            roll < 5 -> RewardResult(
                rewardGrade = SoupRewardGrade.JACKPOT,
                point = 2_000,
            )
            roll < 15 -> RewardResult(
                rewardGrade = SoupRewardGrade.MIDDLE,
                point = 1_000,
            )
            roll < 35 -> RewardResult(
                rewardGrade = SoupRewardGrade.SMALL,
                point = 500,
            )
            roll < 60 -> RewardResult(
                rewardGrade = SoupRewardGrade.INGREDIENT,
                ecoJam = 50,
                rewardedIngredients = listOf(IngredientReward(randomCommonIngredient(), 1)),
            )
            else -> RewardResult(
                rewardGrade = SoupRewardGrade.CONSOLATION,
                ecoJam = 30,
            )
        }
    }

    private fun rewardHiddenSoup(): RewardResult {
        val roll = randomProvider.nextInt(100)
        val baseEcoJam = 300
        val basePoint = 500

        return when {
            roll < 5 -> RewardResult(
                rewardGrade = SoupRewardGrade.JACKPOT,
                ecoJam = baseEcoJam + 200,
                point = basePoint + 2_000,
            )
            roll < 25 -> RewardResult(
                rewardGrade = SoupRewardGrade.MIDDLE,
                ecoJam = baseEcoJam + 100,
                point = basePoint + 1_000,
            )
            roll < 90 -> RewardResult(
                rewardGrade = SoupRewardGrade.SMALL,
                ecoJam = baseEcoJam + 50,
                point = basePoint + 500,
            )
            else -> RewardResult(
                rewardGrade = SoupRewardGrade.INGREDIENT,
                ecoJam = baseEcoJam + 100,
                point = basePoint,
                rewardedIngredients = listOf(IngredientReward(randomHiddenIngredient(), 1)),
            )
        }
    }

    private fun rewardLegendarySoup(): RewardResult {
        val roll = randomProvider.nextInt(100)
        val baseEcoJam = 500
        val basePoint = 1_500

        return when {
            roll < 5 -> RewardResult(
                rewardGrade = SoupRewardGrade.JACKPOT,
                ecoJam = baseEcoJam + 300,
                point = basePoint + 4_000,
            )
            roll < 25 -> RewardResult(
                rewardGrade = SoupRewardGrade.MIDDLE,
                ecoJam = baseEcoJam + 200,
                point = basePoint + 3_000,
            )
            roll < 90 -> RewardResult(
                rewardGrade = SoupRewardGrade.SMALL,
                ecoJam = baseEcoJam + 100,
                point = basePoint + 2_000,
            )
            else -> RewardResult(
                rewardGrade = SoupRewardGrade.INGREDIENT,
                ecoJam = baseEcoJam + 200,
                point = basePoint,
                rewardedIngredients = listOf(
                    IngredientReward(randomHiddenIngredient(), 1),
                    IngredientReward(randomCommonIngredient(), 1),
                    IngredientReward(randomCommonIngredient(), 1),
                ),
            )
        }
    }

    private fun rerollCommonSoup(currentGrade: SoupRewardGrade): RewardResult {
        val roll = randomProvider.nextInt(100)

        return when (currentGrade) {
            SoupRewardGrade.CONSOLATION -> when {
                roll < 60 -> RewardResult(
                    rewardGrade = SoupRewardGrade.INGREDIENT,
                    ecoJam = 50,
                    rewardedIngredients = listOf(IngredientReward(randomCommonIngredient(), 1)),
                )
                roll < 90 -> RewardResult(
                    rewardGrade = SoupRewardGrade.SMALL,
                    point = 500,
                )
                roll < 98 -> RewardResult(
                    rewardGrade = SoupRewardGrade.MIDDLE,
                    point = 1_000,
                )
                else -> RewardResult(
                    rewardGrade = SoupRewardGrade.JACKPOT,
                    point = 2_000,
                )
            }

            SoupRewardGrade.INGREDIENT -> when {
                roll < 65 -> RewardResult(
                    rewardGrade = SoupRewardGrade.INGREDIENT,
                    ecoJam = 50,
                    rewardedIngredients = listOf(IngredientReward(randomCommonIngredient(), 1)),
                )
                roll < 90 -> RewardResult(
                    rewardGrade = SoupRewardGrade.SMALL,
                    point = 500,
                )
                roll < 98 -> RewardResult(
                    rewardGrade = SoupRewardGrade.MIDDLE,
                    point = 1_000,
                )
                else -> RewardResult(
                    rewardGrade = SoupRewardGrade.JACKPOT,
                    point = 2_000,
                )
            }

            SoupRewardGrade.SMALL -> when {
                roll < 75 -> RewardResult(
                    rewardGrade = SoupRewardGrade.SMALL,
                    point = 500,
                )
                roll < 95 -> RewardResult(
                    rewardGrade = SoupRewardGrade.MIDDLE,
                    point = 1_000,
                )
                else -> RewardResult(
                    rewardGrade = SoupRewardGrade.JACKPOT,
                    point = 2_000,
                )
            }

            SoupRewardGrade.MIDDLE -> when {
                roll < 90 -> RewardResult(
                    rewardGrade = SoupRewardGrade.MIDDLE,
                    point = 1_000,
                )
                else -> RewardResult(
                    rewardGrade = SoupRewardGrade.JACKPOT,
                    point = 2_000,
                )
            }

            SoupRewardGrade.JACKPOT -> throw BusinessException(ErrorCode.SOUP_REROLL_NOT_AVAILABLE)
        }
    }

    private fun rerollHiddenSoup(currentGrade: SoupRewardGrade): RewardResult {
        val roll = randomProvider.nextInt(100)
        val baseEcoJam = 300
        val basePoint = 500

        return when (currentGrade) {
            SoupRewardGrade.INGREDIENT -> when {
                roll < 70 -> RewardResult(
                    rewardGrade = SoupRewardGrade.INGREDIENT,
                    ecoJam = baseEcoJam + 100,
                    point = basePoint,
                    rewardedIngredients = listOf(IngredientReward(randomHiddenIngredient(), 1)),
                )
                roll < 90 -> RewardResult(
                    rewardGrade = SoupRewardGrade.SMALL,
                    ecoJam = baseEcoJam + 50,
                    point = basePoint + 500,
                )
                roll < 98 -> RewardResult(
                    rewardGrade = SoupRewardGrade.MIDDLE,
                    ecoJam = baseEcoJam + 100,
                    point = basePoint + 1_000,
                )
                else -> RewardResult(
                    rewardGrade = SoupRewardGrade.JACKPOT,
                    ecoJam = baseEcoJam + 200,
                    point = basePoint + 2_000,
                )
            }

            SoupRewardGrade.SMALL -> when {
                roll < 80 -> RewardResult(
                    rewardGrade = SoupRewardGrade.SMALL,
                    ecoJam = baseEcoJam + 50,
                    point = basePoint + 500,
                )
                roll < 95 -> RewardResult(
                    rewardGrade = SoupRewardGrade.MIDDLE,
                    ecoJam = baseEcoJam + 100,
                    point = basePoint + 1_000,
                )
                else -> RewardResult(
                    rewardGrade = SoupRewardGrade.JACKPOT,
                    ecoJam = baseEcoJam + 200,
                    point = basePoint + 2_000,
                )
            }

            SoupRewardGrade.MIDDLE -> when {
                roll < 92 -> RewardResult(
                    rewardGrade = SoupRewardGrade.MIDDLE,
                    ecoJam = baseEcoJam + 100,
                    point = basePoint + 1_000,
                )
                else -> RewardResult(
                    rewardGrade = SoupRewardGrade.JACKPOT,
                    ecoJam = baseEcoJam + 200,
                    point = basePoint + 2_000,
                )
            }

            SoupRewardGrade.CONSOLATION,
            SoupRewardGrade.JACKPOT,
            -> throw BusinessException(ErrorCode.SOUP_REROLL_NOT_AVAILABLE)
        }
    }

    private fun rerollLegendarySoup(currentGrade: SoupRewardGrade): RewardResult {
        val roll = randomProvider.nextInt(100)
        val baseEcoJam = 500
        val basePoint = 1_500

        return when (currentGrade) {
            SoupRewardGrade.INGREDIENT -> when {
                roll < 75 -> RewardResult(
                    rewardGrade = SoupRewardGrade.INGREDIENT,
                    ecoJam = baseEcoJam + 200,
                    point = basePoint,
                    rewardedIngredients = listOf(
                        IngredientReward(randomHiddenIngredient(), 1),
                        IngredientReward(randomCommonIngredient(), 1),
                        IngredientReward(randomCommonIngredient(), 1),
                    ),
                )
                roll < 93 -> RewardResult(
                    rewardGrade = SoupRewardGrade.SMALL,
                    ecoJam = baseEcoJam + 100,
                    point = basePoint + 2_000,
                )
                roll < 98 -> RewardResult(
                    rewardGrade = SoupRewardGrade.MIDDLE,
                    ecoJam = baseEcoJam + 200,
                    point = basePoint + 3_000,
                )
                else -> RewardResult(
                    rewardGrade = SoupRewardGrade.JACKPOT,
                    ecoJam = baseEcoJam + 300,
                    point = basePoint + 4_000,
                )
            }

            SoupRewardGrade.SMALL -> when {
                roll < 82 -> RewardResult(
                    rewardGrade = SoupRewardGrade.SMALL,
                    ecoJam = baseEcoJam + 100,
                    point = basePoint + 2_000,
                )
                roll < 97 -> RewardResult(
                    rewardGrade = SoupRewardGrade.MIDDLE,
                    ecoJam = baseEcoJam + 200,
                    point = basePoint + 3_000,
                )
                else -> RewardResult(
                    rewardGrade = SoupRewardGrade.JACKPOT,
                    ecoJam = baseEcoJam + 300,
                    point = basePoint + 4_000,
                )
            }

            SoupRewardGrade.MIDDLE -> when {
                roll < 95 -> RewardResult(
                    rewardGrade = SoupRewardGrade.MIDDLE,
                    ecoJam = baseEcoJam + 200,
                    point = basePoint + 3_000,
                )
                else -> RewardResult(
                    rewardGrade = SoupRewardGrade.JACKPOT,
                    ecoJam = baseEcoJam + 300,
                    point = basePoint + 4_000,
                )
            }

            SoupRewardGrade.CONSOLATION,
            SoupRewardGrade.JACKPOT,
            -> throw BusinessException(ErrorCode.SOUP_REROLL_NOT_AVAILABLE)
        }
    }

    private fun applyReward(
        soup: Soup,
        reward: RewardResult,
        ecoJamHistorySourceType: EcoJamHistorySourceType = EcoJamHistorySourceType.SOUP,
        pointHistorySourceType: PointHistorySourceType = PointHistorySourceType.SOUP,
        ingredientHistorySourceType: IngredientHistorySourceType = IngredientHistorySourceType.SOUP,
    ) {
        soup.rewardGrade = reward.rewardGrade
        soup.rewardEcoJam = reward.ecoJam
        soup.rewardPoint = reward.point

        soup.user.increaseEcoJam(reward.ecoJam)
        soup.user.increasePoint(reward.point)
        saveHistories(soup, reward, ecoJamHistorySourceType, pointHistorySourceType)

        reward.rewardedIngredients.forEach { ingredientReward ->
            val userIngredient = userIngredientRepository.findByUserAndIngredient(soup.user, ingredientReward.ingredient)
                .orElseGet {
                    UserIngredient(
                        user = soup.user,
                        ingredient = ingredientReward.ingredient,
                        quantity = 0,
                    )
                }

            userIngredient.increaseQuantity(ingredientReward.quantity)
            userIngredientRepository.save(userIngredient)

            ingredientHistoryRepository.save(
                IngredientHistory.earn(
                    user = soup.user,
                    ingredient = ingredientReward.ingredient,
                    amount = ingredientReward.quantity,
                    sourceType = ingredientHistorySourceType,
                    sourceId = requireNotNull(soup.id),
                ),
            )

            soupRewardIngredientRepository.save(
                SoupRewardIngredient(
                    soup = soup,
                    ingredient = ingredientReward.ingredient,
                    quantity = ingredientReward.quantity,
                ),
            )
        }
    }

    private fun revokeReward(soup: Soup) {
        val soupId = requireNotNull(soup.id)

        if (soup.rewardEcoJam > 0) {
            soup.user.decreaseEcoJam(soup.rewardEcoJam)
            ecoJamHistoryRepository.save(
                EcoJamHistory.spend(
                    user = soup.user,
                    amount = soup.rewardEcoJam,
                    sourceType = EcoJamHistorySourceType.SOUP_REROLL,
                    sourceId = soupId,
                ),
            )
        }

        if (soup.rewardPoint > 0) {
            soup.user.decreasePoint(soup.rewardPoint)
            pointHistoryRepository.save(
                PointHistory.spend(
                    user = soup.user,
                    amount = soup.rewardPoint,
                    sourceType = PointHistorySourceType.SOUP_REROLL,
                    sourceId = soupId,
                ),
            )
        }

        val rewardedIngredients = soupRewardIngredientRepository.findAllBySoupIdOrderByIdAsc(soupId)
        rewardedIngredients.forEach { rewardedIngredient ->
            val userIngredient = userIngredientRepository.findByUserAndIngredient(soup.user, rewardedIngredient.ingredient)
                .orElseThrow { BusinessException(ErrorCode.INSUFFICIENT_INGREDIENT_QUANTITY) }
            userIngredient.decreaseQuantity(rewardedIngredient.quantity)
            ingredientHistoryRepository.save(
                IngredientHistory.spend(
                    user = soup.user,
                    ingredient = rewardedIngredient.ingredient,
                    amount = rewardedIngredient.quantity,
                    sourceType = IngredientHistorySourceType.SOUP_REROLL,
                    sourceId = soupId,
                ),
            )
        }
        soupRewardIngredientRepository.deleteAll(rewardedIngredients)
    }

    private fun saveHistories(
        soup: Soup,
        reward: RewardResult,
        ecoJamHistorySourceType: EcoJamHistorySourceType,
        pointHistorySourceType: PointHistorySourceType,
    ) {
        val soupId = requireNotNull(soup.id)

        if (reward.ecoJam > 0) {
            ecoJamHistoryRepository.save(
                EcoJamHistory.earn(
                    user = soup.user,
                    amount = reward.ecoJam,
                    sourceType = ecoJamHistorySourceType,
                    sourceId = soupId,
                ),
            )
        }

        if (reward.point > 0) {
            pointHistoryRepository.save(
                PointHistory.earn(
                    user = soup.user,
                    amount = reward.point,
                    sourceType = pointHistorySourceType,
                    sourceId = soupId,
                ),
            )
        }
    }

    private fun randomCommonIngredient(): Ingredient = ingredientRepository.findAllByType(IngredientType.COMMON)
        .takeIf { it.isNotEmpty() }
        ?.let { ingredients -> ingredients[randomProvider.nextInt(ingredients.size)] }
        ?: throw BusinessException(ErrorCode.INGREDIENT_NOT_FOUND)

    private fun randomHiddenIngredient(): Ingredient = ingredientRepository.findAllByType(IngredientType.HIDDEN)
        .takeIf { it.isNotEmpty() }
        ?.let { ingredients -> ingredients[randomProvider.nextInt(ingredients.size)] }
        ?: throw BusinessException(ErrorCode.INGREDIENT_NOT_FOUND)

    private data class RewardResult(
        val rewardGrade: SoupRewardGrade,
        val ecoJam: Int = 0,
        val point: Int = 0,
        val rewardedIngredients: List<IngredientReward> = emptyList(),
    )

    private data class IngredientReward(
        val ingredient: Ingredient,
        val quantity: Int,
    )
}
