package com.zerost.api.soup.application

import com.zerost.api.common.exception.BusinessException
import com.zerost.api.common.exception.ErrorCode
import com.zerost.api.ingredient.domain.Ingredient
import com.zerost.api.ingredient.domain.IngredientRepository
import com.zerost.api.ingredient.domain.IngredientType
import com.zerost.api.ingredient.domain.UserIngredient
import com.zerost.api.ingredient.domain.UserIngredientRepository
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
            almangPoint = reward.almangPoint,
            rewardedIngredients = reward.rewardedIngredients.map { ingredientReward ->
                SoupRewardIngredientResponse(
                    ingredientId = requireNotNull(ingredientReward.ingredient.id),
                    ingredientName = ingredientReward.ingredient.name,
                    quantity = ingredientReward.quantity,
                )
            },
        )
    }

    private fun rewardCommonSoup(): RewardResult {
        val roll = randomProvider.nextInt(100)

        return when {
            roll < 5 -> RewardResult(
                rewardGrade = SoupRewardGrade.JACKPOT,
                almangPoint = 2_000,
            )
            roll < 15 -> RewardResult(
                rewardGrade = SoupRewardGrade.MIDDLE,
                almangPoint = 1_000,
            )
            roll < 35 -> RewardResult(
                rewardGrade = SoupRewardGrade.SMALL,
                almangPoint = 500,
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
                almangPoint = basePoint + 2_000,
            )
            roll < 25 -> RewardResult(
                rewardGrade = SoupRewardGrade.MIDDLE,
                ecoJam = baseEcoJam + 100,
                almangPoint = basePoint + 1_000,
            )
            roll < 90 -> RewardResult(
                rewardGrade = SoupRewardGrade.SMALL,
                ecoJam = baseEcoJam + 50,
                almangPoint = basePoint + 500,
            )
            else -> RewardResult(
                rewardGrade = SoupRewardGrade.INGREDIENT,
                ecoJam = baseEcoJam + 100,
                almangPoint = basePoint,
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
                almangPoint = basePoint + 4_000,
            )
            roll < 25 -> RewardResult(
                rewardGrade = SoupRewardGrade.MIDDLE,
                ecoJam = baseEcoJam + 200,
                almangPoint = basePoint + 3_000,
            )
            roll < 90 -> RewardResult(
                rewardGrade = SoupRewardGrade.SMALL,
                ecoJam = baseEcoJam + 100,
                almangPoint = basePoint + 2_000,
            )
            else -> RewardResult(
                rewardGrade = SoupRewardGrade.INGREDIENT,
                ecoJam = baseEcoJam + 200,
                almangPoint = basePoint,
                rewardedIngredients = listOf(
                    IngredientReward(randomHiddenIngredient(), 1),
                    IngredientReward(randomCommonIngredient(), 1),
                    IngredientReward(randomCommonIngredient(), 1),
                ),
            )
        }
    }

    private fun applyReward(soup: Soup, reward: RewardResult) {
        soup.rewardGrade = reward.rewardGrade
        soup.rewardEcoJam = reward.ecoJam
        soup.rewardAlmangPoint = reward.almangPoint

        soup.user.increaseEcoJam(reward.ecoJam)
        soup.user.increaseAlmangPoint(reward.almangPoint)

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

            soupRewardIngredientRepository.save(
                SoupRewardIngredient(
                    soup = soup,
                    ingredient = ingredientReward.ingredient,
                    quantity = ingredientReward.quantity,
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
        val almangPoint: Int = 0,
        val rewardedIngredients: List<IngredientReward> = emptyList(),
    )

    private data class IngredientReward(
        val ingredient: Ingredient,
        val quantity: Int,
    )
}
