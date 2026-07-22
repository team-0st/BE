package com.zerost.api.recipe.application

import com.zerost.api.common.exception.BusinessException
import com.zerost.api.common.exception.ErrorCode
import com.zerost.api.recipe.domain.Recipe
import com.zerost.api.recipe.domain.WeeklyRecipeSelectionRepository
import org.springframework.stereotype.Service
import java.time.Clock
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

@Service
class WeeklyRecipeSelectionService(
    private val weeklyRecipeSelectionRepository: WeeklyRecipeSelectionRepository,
    private val weeklyRecipeSelectionProvisionService: WeeklyRecipeSelectionProvisionService,
    private val clock: Clock,
) {

    fun getCurrentWeeklyRecipe(): Recipe? {
        val weekStartDate = currentWeekStartDate()
        val existingSelection = weeklyRecipeSelectionRepository.findByWeekStartDate(weekStartDate)
        if (existingSelection != null) {
            return existingSelection.recipe
        }

        return weeklyRecipeSelectionProvisionService.createOrLoad(weekStartDate)
    }

    private fun currentWeekStartDate(): LocalDate =
        LocalDate.now(clock).with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
}
