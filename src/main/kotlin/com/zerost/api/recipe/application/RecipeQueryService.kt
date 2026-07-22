package com.zerost.api.recipe.application

import com.zerost.api.common.exception.BusinessException
import com.zerost.api.common.exception.ErrorCode
import com.zerost.api.recipe.domain.Recipe
import com.zerost.api.recipe.domain.RecipeHint
import com.zerost.api.recipe.domain.RecipeHintRepository
import com.zerost.api.recipe.domain.RecipeIngredientRepository
import com.zerost.api.recipe.domain.RecipeRepository
import com.zerost.api.recipe.domain.RecipeType
import com.zerost.api.recipe.domain.UserUnlockedRecipeRepository
import com.zerost.api.recipe.presentation.dto.RecipeDetailIngredientResponse
import com.zerost.api.recipe.presentation.dto.RecipeDetailResponse
import com.zerost.api.recipe.presentation.dto.RecipeHintResponse
import com.zerost.api.recipe.presentation.dto.RecipeSectionsResponse
import com.zerost.api.recipe.presentation.dto.RecipeSummaryResponse
import com.zerost.api.user.domain.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class RecipeQueryService(
    private val userRepository: UserRepository,
    private val recipeRepository: RecipeRepository,
    private val recipeHintRepository: RecipeHintRepository,
    private val recipeIngredientRepository: RecipeIngredientRepository,
    private val userUnlockedRecipeRepository: UserUnlockedRecipeRepository,
    private val weeklyRecipeSelectionService: WeeklyRecipeSelectionService,
) {

    @Transactional(readOnly = true)
    fun getRecipes(userId: Long): RecipeSectionsResponse {
        val user = getUser(userId)
        val unlockedRecipeIds = userUnlockedRecipeRepository.findRecipeIdsByUserId(requireNotNull(user.id)).toSet()
        val recipes = recipeRepository.findAllByOrderByIdAsc()
        val hintsByRecipeId = loadHintsByRecipeId(recipes.mapNotNull { it.id })

        return RecipeSectionsResponse(
            introRecipes = recipes
                .filter { it.intro }
                .map { it.toSummaryResponse(unlockedRecipeIds, hintsByRecipeId) },
            weeklyRecipe = weeklyRecipeSelectionService.getCurrentWeeklyRecipe()
                ?.toSummaryResponse(unlockedRecipeIds, hintsByRecipeId),
            hiddenRecipes = recipes
                .filter { it.hidden }
                .map { it.toSummaryResponse(unlockedRecipeIds, hintsByRecipeId) },
        )
    }

    @Transactional(readOnly = true)
    fun getRecipe(userId: Long, recipeId: Long): RecipeDetailResponse {
        val user = getUser(userId)
        val unlockedRecipeIds = userUnlockedRecipeRepository.findRecipeIdsByUserId(requireNotNull(user.id)).toSet()

        val recipe = recipeRepository.findById(recipeId)
            .orElseThrow { BusinessException(ErrorCode.RECIPE_NOT_FOUND) }
        val hints = recipeHintRepository.findAllByRecipeIdOrderByIdAsc(recipeId)
            .map { it.toResponse() }
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
            hints = hints,
            ingredients = ingredients,
        )
    }

    private fun getUser(userId: Long) =
        userRepository.findById(userId)
            .orElseThrow { BusinessException(ErrorCode.USER_NOT_FOUND) }

    private fun Recipe.getDisplayName(unlockedRecipeIds: Set<Long>): String =
        if (isVisible(unlockedRecipeIds)) name else MASKED_RECIPE_NAME

    private fun loadHintsByRecipeId(recipeIds: List<Long>): Map<Long, List<RecipeHintResponse>> {
        if (recipeIds.isEmpty()) {
            return emptyMap()
        }

        return recipeHintRepository.findAllByRecipeIdInOrderByRecipeIdAscIdAsc(recipeIds)
            .groupBy { requireNotNull(it.recipe.id) }
            .mapValues { (_, hints) -> hints.map { it.toResponse() } }
    }

    private fun Recipe.toSummaryResponse(
        unlockedRecipeIds: Set<Long>,
        hintsByRecipeId: Map<Long, List<RecipeHintResponse>>,
    ) =
        RecipeSummaryResponse(
            recipeId = requireNotNull(id),
            name = getDisplayName(unlockedRecipeIds),
            type = type.name,
            slotCount = slotCount,
            recipeVisible = isVisible(unlockedRecipeIds),
            hints = hintsByRecipeId[requireNotNull(id)].orEmpty(),
        )

    private fun RecipeHint.toResponse() =
        RecipeHintResponse(
            level = hintLevel.name,
            content = content,
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
