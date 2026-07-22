package com.zerost.api.recipe.application

import com.zerost.api.common.exception.BusinessException
import com.zerost.api.common.exception.ErrorCode
import com.zerost.api.recipe.domain.Recipe
import com.zerost.api.recipe.domain.RecipeIngredientRepository
import com.zerost.api.recipe.domain.RecipeRepository
import com.zerost.api.recipe.domain.RecipeType
import com.zerost.api.recipe.domain.UserUnlockedRecipeRepository
import com.zerost.api.recipe.presentation.dto.RecipeDetailIngredientResponse
import com.zerost.api.recipe.presentation.dto.RecipeDetailResponse
import com.zerost.api.recipe.presentation.dto.RecipeSectionsResponse
import com.zerost.api.recipe.presentation.dto.RecipeSummaryResponse
import com.zerost.api.user.domain.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class RecipeQueryService(
    private val userRepository: UserRepository,
    private val recipeRepository: RecipeRepository,
    private val recipeIngredientRepository: RecipeIngredientRepository,
    private val userUnlockedRecipeRepository: UserUnlockedRecipeRepository,
) {

    @Transactional(readOnly = true)
    fun getRecipes(userId: Long): RecipeSectionsResponse {
        val user = getUser(userId)
        val unlockedRecipeIds = userUnlockedRecipeRepository.findRecipeIdsByUserId(requireNotNull(user.id)).toSet()
        val recipes = recipeRepository.findAllByOrderByIdAsc()

        return RecipeSectionsResponse(
            introRecipes = recipes
                .filter { it.intro }
                .map { it.toSummaryResponse(unlockedRecipeIds) },
            weeklyRecipe = recipes
                .firstOrNull { it.weekly }
                ?.toSummaryResponse(unlockedRecipeIds),
            hiddenRecipes = recipes
                .filter { it.hidden }
                .map { it.toSummaryResponse(unlockedRecipeIds) },
        )
    }

    @Transactional(readOnly = true)
    fun getRecipe(userId: Long, recipeId: Long): RecipeDetailResponse {
        val user = getUser(userId)
        val unlockedRecipeIds = userUnlockedRecipeRepository.findRecipeIdsByUserId(requireNotNull(user.id)).toSet()

        val recipe = recipeRepository.findById(recipeId)
            .orElseThrow { BusinessException(ErrorCode.RECIPE_NOT_FOUND) }
        val ingredients = if (recipe.isVisible(unlockedRecipeIds)) {
            recipeIngredientRepository.findAllByRecipeIdOrderBySlotOrderAsc(recipeId)
                .map { recipeIngredient ->
                    RecipeDetailIngredientResponse(
                        ingredientId = requireNotNull(recipeIngredient.ingredient.id),
                        name = recipeIngredient.ingredient.name,
                        type = recipeIngredient.ingredient.type.name,
                        imageUrl = recipeIngredient.ingredient.imageUrl,
                        slotOrder = recipeIngredient.slotOrder,
                    )
                }
        } else {
            emptyList()
        }

        return RecipeDetailResponse(
            recipeId = recipeId,
            name = recipe.getDisplayName(unlockedRecipeIds),
            type = recipe.type.name,
            slotCount = recipe.slotCount,
            recipeVisible = recipe.isVisible(unlockedRecipeIds),
            ingredients = ingredients,
        )
    }

    private fun getUser(userId: Long) =
        userRepository.findById(userId)
            .orElseThrow { BusinessException(ErrorCode.USER_NOT_FOUND) }

    private fun Recipe.getDisplayName(unlockedRecipeIds: Set<Long>): String =
        if (isVisible(unlockedRecipeIds)) name else MASKED_RECIPE_NAME

    private fun Recipe.toSummaryResponse(unlockedRecipeIds: Set<Long>) =
        RecipeSummaryResponse(
            recipeId = requireNotNull(id),
            name = getDisplayName(unlockedRecipeIds),
            type = type.name,
            slotCount = slotCount,
            recipeVisible = isVisible(unlockedRecipeIds),
        )

    private fun Recipe.isVisible(unlockedRecipeIds: Set<Long>): Boolean {
        if (!hidden) {
            return true
        }

        return type == RecipeType.HIDDEN && requireNotNull(id) in unlockedRecipeIds
    }

    companion object {
        private const val MASKED_RECIPE_NAME = "???"
    }
}
