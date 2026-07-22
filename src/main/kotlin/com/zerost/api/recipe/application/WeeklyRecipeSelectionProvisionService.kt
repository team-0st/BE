package com.zerost.api.recipe.application

import com.zerost.api.common.exception.BusinessException
import com.zerost.api.common.exception.ErrorCode
import com.zerost.api.recipe.domain.Recipe
import com.zerost.api.recipe.domain.RecipeRepository
import com.zerost.api.recipe.domain.RecipeType
import com.zerost.api.recipe.domain.WeeklyRecipeSelection
import com.zerost.api.recipe.domain.WeeklyRecipeSelectionRepository
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Service
class WeeklyRecipeSelectionProvisionService(
    private val recipeRepository: RecipeRepository,
    private val weeklyRecipeSelectionRepository: WeeklyRecipeSelectionRepository,
    private val weeklyRecipeSelectionRandomProvider: WeeklyRecipeSelectionRandomProvider,
) {

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun createOrLoad(weekStartDate: LocalDate): Recipe? {
        weeklyRecipeSelectionRepository.findByWeekStartDate(weekStartDate)?.let { return it.recipe }

        val candidates = recipeRepository.findAllByTypeAndIntroFalseAndHiddenFalseOrderByIdAsc(RecipeType.COMMON)
        if (candidates.isEmpty()) {
            throw BusinessException(ErrorCode.INVALID_WEEKLY_RECIPE_SELECTION)
        }

        val selectedRecipe = candidates[weeklyRecipeSelectionRandomProvider.nextInt(candidates.size)]

        return try {
            weeklyRecipeSelectionRepository.saveAndFlush(
                WeeklyRecipeSelection(
                    weekStartDate = weekStartDate,
                    recipe = selectedRecipe,
                ),
            )
            selectedRecipe
        } catch (_: DataIntegrityViolationException) {
            weeklyRecipeSelectionRepository.findByWeekStartDate(weekStartDate)?.recipe
                ?: throw BusinessException(ErrorCode.INVALID_WEEKLY_RECIPE_SELECTION)
        }
    }
}
