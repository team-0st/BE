package com.zerost.api.recipe.application

import com.zerost.api.common.exception.BusinessException
import com.zerost.api.common.exception.ErrorCode
import com.zerost.api.ecojam.domain.EcoJamHistory
import com.zerost.api.ecojam.domain.EcoJamHistoryRepository
import com.zerost.api.ecojam.domain.EcoJamHistorySourceType
import com.zerost.api.recipe.domain.RecipeRepository
import com.zerost.api.recipe.domain.RecipeType
import com.zerost.api.recipe.domain.UserUnlockedRecipe
import com.zerost.api.recipe.domain.UserUnlockedRecipeRepository
import com.zerost.api.recipe.presentation.dto.UnlockHiddenRecipeResponse
import com.zerost.api.user.domain.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class RecipeUnlockService(
    private val userRepository: UserRepository,
    private val recipeRepository: RecipeRepository,
    private val userUnlockedRecipeRepository: UserUnlockedRecipeRepository,
    private val ecoJamHistoryRepository: EcoJamHistoryRepository,
    private val recipeUnlockRandomProvider: RecipeUnlockRandomProvider,
) {

    @Transactional
    fun unlockRandomHiddenRecipe(deviceId: String): UnlockHiddenRecipeResponse {
        val user = userRepository.findByDeviceIdForUpdate(deviceId)
            .orElseThrow { BusinessException(ErrorCode.USER_NOT_FOUND) }

        val hiddenRecipes = recipeRepository.findAllByTypeOrderByIdAsc(RecipeType.HIDDEN)
        val unlockedRecipeIds = userUnlockedRecipeRepository.findRecipeIdsByUserId(requireNotNull(user.id)).toSet()
        val unlockableRecipes = hiddenRecipes.filter { recipe ->
            requireNotNull(recipe.id) !in unlockedRecipeIds
        }

        if (unlockableRecipes.isEmpty()) {
            throw BusinessException(ErrorCode.ALL_HIDDEN_RECIPES_ALREADY_UNLOCKED)
        }

        if (user.ecoJam < UNLOCK_COST_ECO_JAM) {
            throw BusinessException(ErrorCode.INSUFFICIENT_ECO_JAM)
        }

        val unlockedRecipe = unlockableRecipes[recipeUnlockRandomProvider.nextInt(unlockableRecipes.size)]
        user.decreaseEcoJam(UNLOCK_COST_ECO_JAM)

        userUnlockedRecipeRepository.save(
            UserUnlockedRecipe(
                user = user,
                recipe = unlockedRecipe,
            ),
        )

        ecoJamHistoryRepository.save(
            EcoJamHistory.spend(
                user = user,
                amount = UNLOCK_COST_ECO_JAM,
                sourceType = EcoJamHistorySourceType.RECIPE_UNLOCK,
                sourceId = requireNotNull(unlockedRecipe.id),
            ),
        )

        return UnlockHiddenRecipeResponse(
            recipeId = requireNotNull(unlockedRecipe.id),
            recipeName = unlockedRecipe.name,
            remainingEcoJam = user.ecoJam,
        )
    }

    companion object {
        private const val UNLOCK_COST_ECO_JAM = 500
    }
}
