package com.zerost.api.recipe.application

import com.zerost.api.common.exception.BusinessException
import com.zerost.api.common.exception.ErrorCode
import com.zerost.api.recipe.domain.Recipe
import com.zerost.api.recipe.domain.RecipeIngredientRepository
import com.zerost.api.recipe.domain.RecipeRepository
import com.zerost.api.recipe.presentation.dto.RecipeDetailIngredientResponse
import com.zerost.api.recipe.presentation.dto.RecipeDetailResponse
import com.zerost.api.recipe.presentation.dto.RecipeSummaryResponse
import com.zerost.api.user.domain.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class RecipeQueryService(
    private val userRepository: UserRepository,
    private val recipeRepository: RecipeRepository,
    private val recipeIngredientRepository: RecipeIngredientRepository,
) {

    @Transactional(readOnly = true)
    fun getRecipes(deviceId: String): List<RecipeSummaryResponse> {
        validateUser(deviceId)

        return recipeRepository.findAllByOrderByIdAsc()
            .map { recipe ->
                RecipeSummaryResponse(
                    recipeId = requireNotNull(recipe.id),
                    name = recipe.getDisplayName(),
                    type = recipe.type.name,
                    slotCount = recipe.slotCount,
                    recipeVisible = recipe.isVisible(),
                )
            }
    }

    @Transactional(readOnly = true)
    fun getRecipe(deviceId: String, recipeId: Long): RecipeDetailResponse {
        validateUser(deviceId)

        val recipe = recipeRepository.findById(recipeId)
            .orElseThrow { BusinessException(ErrorCode.RECIPE_NOT_FOUND) }
        val ingredients = if (recipe.isVisible()) {
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
            name = recipe.getDisplayName(),
            type = recipe.type.name,
            slotCount = recipe.slotCount,
            recipeVisible = recipe.isVisible(),
            ingredients = ingredients,
        )
    }

    private fun validateUser(deviceId: String) {
        userRepository.findByDeviceId(deviceId)
            .orElseThrow { BusinessException(ErrorCode.USER_NOT_FOUND) }
    }

    private fun Recipe.getDisplayName(): String =
        if (hidden) MASKED_RECIPE_NAME else name

    private fun Recipe.isVisible(): Boolean = !hidden

    companion object {
        private const val MASKED_RECIPE_NAME = "???"
    }
}
