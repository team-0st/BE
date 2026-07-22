package com.zerost.api.recipe.application

import com.zerost.api.common.exception.BusinessException
import com.zerost.api.common.exception.ErrorCode
import com.zerost.api.recipe.domain.RecipeRepository
import com.zerost.api.recipe.domain.RecipeType
import com.zerost.api.recipe.domain.WeeklyRecipeSelection
import com.zerost.api.recipe.domain.WeeklyRecipeSelectionRepository
import com.zerost.api.support.createRecipe
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.Mockito.`when`
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
import kotlin.test.assertEquals

class WeeklyRecipeSelectionServiceTest {

    private val recipeRepository = mock(RecipeRepository::class.java)
    private val weeklyRecipeSelectionRepository = mock(WeeklyRecipeSelectionRepository::class.java)
    private val weeklyRecipeSelectionRandomProvider = mock(WeeklyRecipeSelectionRandomProvider::class.java)
    private val weeklyRecipeSelectionService = WeeklyRecipeSelectionService(
        recipeRepository = recipeRepository,
        weeklyRecipeSelectionRepository = weeklyRecipeSelectionRepository,
        weeklyRecipeSelectionRandomProvider = weeklyRecipeSelectionRandomProvider,
    )

    @Test
    fun `이번 주 레시피가 이미 존재하면 기존 편성을 반환한다`() {
        val recipe = createRecipe(id = 2L, name = "오리지널 스프", type = RecipeType.COMMON)
        val weekStartDate = currentWeekStartDate()
        `when`(weeklyRecipeSelectionRepository.findByWeekStartDate(weekStartDate)).thenReturn(
            WeeklyRecipeSelection(
                id = 1L,
                weekStartDate = weekStartDate,
                recipe = recipe,
            ),
        )

        val response = weeklyRecipeSelectionService.getCurrentWeeklyRecipe()

        assertEquals("오리지널 스프", response?.name)
        verifyNoInteractions(recipeRepository)
    }

    @Test
    fun `이번 주 레시피가 없으면 일반 레시피 중 하나를 선택해 저장한다`() {
        val recipe1 = createRecipe(id = 1L, name = "오리지널 스프", type = RecipeType.COMMON)
        val recipe2 = createRecipe(id = 2L, name = "채소 스프", type = RecipeType.COMMON)
        val weekStartDate = currentWeekStartDate()
        `when`(weeklyRecipeSelectionRepository.findByWeekStartDate(weekStartDate)).thenReturn(null)
        `when`(recipeRepository.findAllByTypeAndIntroFalseAndHiddenFalseOrderByIdAsc(RecipeType.COMMON)).thenReturn(
            listOf(recipe1, recipe2),
        )
        `when`(weeklyRecipeSelectionRandomProvider.nextInt(2)).thenReturn(1)

        val response = weeklyRecipeSelectionService.getCurrentWeeklyRecipe()

        assertEquals("채소 스프", response?.name)
        verify(weeklyRecipeSelectionRepository).save(any(WeeklyRecipeSelection::class.java))
    }

    @Test
    fun `편성 가능한 일반 레시피가 없으면 예외가 발생한다`() {
        val weekStartDate = currentWeekStartDate()
        `when`(weeklyRecipeSelectionRepository.findByWeekStartDate(weekStartDate)).thenReturn(null)
        `when`(recipeRepository.findAllByTypeAndIntroFalseAndHiddenFalseOrderByIdAsc(RecipeType.COMMON)).thenReturn(emptyList())

        val exception = assertThrows<BusinessException> {
            weeklyRecipeSelectionService.getCurrentWeeklyRecipe()
        }

        assertEquals(ErrorCode.INVALID_WEEKLY_RECIPE_SELECTION, exception.errorCode)
    }

    private fun currentWeekStartDate(): LocalDate =
        LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
}
