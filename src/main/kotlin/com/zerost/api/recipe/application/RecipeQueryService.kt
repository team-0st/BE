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
import com.zerost.api.soup.domain.SoupRepository
import com.zerost.api.user.domain.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class RecipeQueryService(
    private val userRepository: UserRepository,
    private val recipeRepository: RecipeRepository,
    private val recipeIngredientRepository: RecipeIngredientRepository,
    private val userUnlockedRecipeRepository: UserUnlockedRecipeRepository,
    private val soupRepository: SoupRepository,
) {

    @Transactional(readOnly = true)
    fun getRecipes(userId: Long): RecipeSectionsResponse {
        val user = getUser(userId)
        val unlockedRecipeIds = userUnlockedRecipeRepository.findRecipeIdsByUserId(requireNotNull(user.id)).toSet()
        val brewedRecipeIds = soupRepository.findDistinctRecipeIdsByUserId(requireNotNull(user.id)).toSet()
        val recipes = recipeRepository.findAllByOrderByIdAsc()

        return RecipeSectionsResponse(
            introRecipes = recipes
                .filter { it.intro }
                .map { it.toSummaryResponse(unlockedRecipeIds, brewedRecipeIds) },
            generalRecipes = recipes
                .filter { it.type == RecipeType.COMMON && !it.intro }
                .map { it.toSummaryResponse(unlockedRecipeIds, brewedRecipeIds) },
            hiddenRecipes = recipes
                .filter { it.type == RecipeType.HIDDEN && !it.intro }
                .map { it.toSummaryResponse(unlockedRecipeIds, brewedRecipeIds) },
            legendaryRecipes = recipes
                .filter { it.type == RecipeType.LEGENDARY && !it.intro }
                .map { it.toSummaryResponse(unlockedRecipeIds, brewedRecipeIds) },
        )
    }

    @Transactional(readOnly = true)
    fun getRecipe(userId: Long, recipeId: Long): RecipeDetailResponse {
        val user = getUser(userId)
        val unlockedRecipeIds = userUnlockedRecipeRepository.findRecipeIdsByUserId(requireNotNull(user.id)).toSet()
        val brewedRecipeIds = soupRepository.findDistinctRecipeIdsByUserId(requireNotNull(user.id)).toSet()

        val recipe = recipeRepository.findById(recipeId)
            .orElseThrow { BusinessException(ErrorCode.RECIPE_NOT_FOUND) }
        val ingredients = if (recipe.isVisible(unlockedRecipeIds, brewedRecipeIds)) {
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
            name = recipe.getDisplayName(unlockedRecipeIds, brewedRecipeIds),
            type = recipe.type.name,
            slotCount = recipe.slotCount,
            recipeVisible = recipe.isVisible(unlockedRecipeIds, brewedRecipeIds),
            ingredients = ingredients,
        )
    }

    private fun getUser(userId: Long) =
        userRepository.findById(userId)
            .orElseThrow { BusinessException(ErrorCode.USER_NOT_FOUND) }

    private fun Recipe.getDisplayName(
        unlockedRecipeIds: Set<Long>,
        brewedRecipeIds: Set<Long>,
    ): String = if (isVisible(unlockedRecipeIds, brewedRecipeIds)) name else MASKED_RECIPE_NAME

    private fun Recipe.toSummaryResponse(
        unlockedRecipeIds: Set<Long>,
        brewedRecipeIds: Set<Long>,
    ) =
        RecipeSummaryResponse(
            recipeId = requireNotNull(id),
            name = getDisplayName(unlockedRecipeIds, brewedRecipeIds),
            type = type.name,
            slotCount = slotCount,
            recipeVisible = isVisible(unlockedRecipeIds, brewedRecipeIds),
        )

    private fun Recipe.isVisible(
        unlockedRecipeIds: Set<Long>,
        brewedRecipeIds: Set<Long>,
    ): Boolean {
        val recipeId = requireNotNull(id)
        return when (type) {
            RecipeType.HIDDEN -> recipeId in unlockedRecipeIds || recipeId in brewedRecipeIds
            RecipeType.LEGENDARY -> recipeId in brewedRecipeIds
            else -> true
        }
    }

    companion object {
        private const val MASKED_RECIPE_NAME = "???"
    }
}
