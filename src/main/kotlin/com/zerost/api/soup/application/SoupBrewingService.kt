package com.zerost.api.soup.application

import com.zerost.api.common.exception.BusinessException
import com.zerost.api.common.exception.ErrorCode
import com.zerost.api.ingredient.domain.UserIngredientRepository
import com.zerost.api.recipe.domain.Recipe
import com.zerost.api.recipe.domain.RecipeIngredientRepository
import com.zerost.api.recipe.domain.RecipeRepository
import com.zerost.api.soup.domain.Soup
import com.zerost.api.soup.domain.SoupRewardGrade
import com.zerost.api.soup.domain.SoupRepository
import com.zerost.api.soup.presentation.dto.BrewSoupResponse
import com.zerost.api.user.domain.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class SoupBrewingService(
    private val userRepository: UserRepository,
    private val recipeRepository: RecipeRepository,
    private val recipeIngredientRepository: RecipeIngredientRepository,
    private val userIngredientRepository: UserIngredientRepository,
    private val soupRepository: SoupRepository,
    private val soupRewardService: SoupRewardService,
) {

    @Transactional
    fun brew(userId: Long, ingredientIds: List<Long>): BrewSoupResponse {
        validateSlotCount(ingredientIds)

        val user = userRepository.findByIdForUpdate(userId)
            .orElseThrow { BusinessException(ErrorCode.USER_NOT_FOUND) }

        val recipe = findMatchingRecipe(ingredientIds)
        consumeIngredients(requireNotNull(user.id), ingredientIds)

        val soup = soupRepository.save(
            Soup(
                user = user,
                recipe = recipe,
                rewardGrade = SoupRewardGrade.CONSOLATION,
            ),
        )
        val reward = soupRewardService.reward(soup)

        return BrewSoupResponse(
            soupId = requireNotNull(soup.id),
            recipeId = requireNotNull(recipe.id),
            recipeName = recipe.name,
            recipeType = recipe.type.name,
            rewardGrade = reward.rewardGrade,
            rewardEcoJam = reward.ecoJam,
            rewardPoint = reward.point,
            rewardedIngredients = reward.rewardedIngredients,
            baseReward = reward.baseReward,
            bonusReward = reward.bonusReward,
        )
    }

    private fun validateSlotCount(ingredientIds: List<Long>) {
        if (ingredientIds.size !in 2..5) {
            throw BusinessException(ErrorCode.INVALID_SOUP_SLOT_COUNT)
        }
    }

    private fun findMatchingRecipe(ingredientIds: List<Long>): Recipe {
        val candidates = recipeRepository.findAllBySlotCountOrderByIdAsc(ingredientIds.size)
        if (candidates.isEmpty()) {
            throw BusinessException(ErrorCode.SOUP_RECIPE_NOT_FOUND)
        }

        val recipeIngredientMap = recipeIngredientRepository
            .findAllByRecipeIdInOrderByRecipeIdAscSlotOrderAsc(candidates.map { requireNotNull(it.id) })
            .groupBy { requireNotNull(it.recipe.id) }
            .mapValues { (_, values) -> values.map { requireNotNull(it.ingredient.id) } }

        return candidates.firstOrNull { candidate ->
            recipeIngredientMap[requireNotNull(candidate.id)] == ingredientIds
        } ?: throw BusinessException(ErrorCode.SOUP_RECIPE_NOT_FOUND)
    }

    private fun consumeIngredients(userId: Long, ingredientIds: List<Long>) {
        val userIngredientMap = userIngredientRepository
            .findAllByUserIdAndIngredientIdIn(userId, ingredientIds.distinct())
            .associateBy { requireNotNull(it.ingredient.id) }

        ingredientIds.forEach { ingredientId ->
            val userIngredient = userIngredientMap[ingredientId]
                ?: throw BusinessException(ErrorCode.INSUFFICIENT_INGREDIENT_QUANTITY)
            userIngredient.decreaseQuantity()
        }
    }
}
