package com.zerost.api.recipe.application

import com.zerost.api.common.exception.BusinessException
import com.zerost.api.common.exception.ErrorCode
import com.zerost.api.ingredient.domain.IngredientType
import com.zerost.api.recipe.domain.RecipeIngredientRepository
import com.zerost.api.recipe.domain.RecipeRepository
import com.zerost.api.recipe.domain.RecipeType
import com.zerost.api.recipe.domain.UserUnlockedRecipeRepository
import com.zerost.api.support.createIngredient
import com.zerost.api.support.createRecipe
import com.zerost.api.support.createRecipeIngredient
import com.zerost.api.support.createUser
import com.zerost.api.user.domain.UserRepository
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import java.util.Optional
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RecipeQueryServiceTest {

    private val userRepository = mock(UserRepository::class.java)
    private val recipeRepository = mock(RecipeRepository::class.java)
    private val recipeIngredientRepository = mock(RecipeIngredientRepository::class.java)
    private val userUnlockedRecipeRepository = mock(UserUnlockedRecipeRepository::class.java)
    private val weeklyRecipeSelectionService = mock(WeeklyRecipeSelectionService::class.java)
    private val recipeQueryService = RecipeQueryService(
        userRepository = userRepository,
        recipeRepository = recipeRepository,
        recipeIngredientRepository = recipeIngredientRepository,
        userUnlockedRecipeRepository = userUnlockedRecipeRepository,
        weeklyRecipeSelectionService = weeklyRecipeSelectionService,
    )

    @Test
    fun `레시피 목록 조회 시 비공개 레시피 이름은 마스킹된다`() {
        `when`(userRepository.findById(1L)).thenReturn(Optional.of(createUser()))
        `when`(userUnlockedRecipeRepository.findRecipeIdsByUserId(1L)).thenReturn(emptyList())
        `when`(weeklyRecipeSelectionService.getCurrentWeeklyRecipe()).thenReturn(
            createRecipe(id = 2L, name = "오리지널 스프", type = RecipeType.COMMON),
        )
        `when`(recipeRepository.findAllByOrderByIdAsc()).thenReturn(
            listOf(
                createRecipe(id = 1L, name = "따뜻한 입문 스프", type = RecipeType.COMMON, intro = true),
                createRecipe(id = 3L, name = "크리스탈 스프", type = RecipeType.HIDDEN, hidden = true),
            ),
        )

        val response = recipeQueryService.getRecipes(1L)

        assertEquals(1, response.introRecipes.size)
        assertEquals("따뜻한 입문 스프", response.introRecipes[0].name)
        assertTrue(response.introRecipes[0].recipeVisible)
        assertEquals("오리지널 스프", response.weeklyRecipe?.name)
        assertEquals("???", response.hiddenRecipes[0].name)
        assertEquals("HIDDEN", response.hiddenRecipes[0].type)
        assertEquals(false, response.hiddenRecipes[0].recipeVisible)
    }

    @Test
    fun `레시피 목록 조회 시 입문과 이번주에 해당하지 않는 일반 레시피는 목록에서 제외된다`() {
        `when`(userRepository.findById(1L)).thenReturn(Optional.of(createUser()))
        `when`(userUnlockedRecipeRepository.findRecipeIdsByUserId(1L)).thenReturn(emptyList())
        `when`(weeklyRecipeSelectionService.getCurrentWeeklyRecipe()).thenReturn(
            createRecipe(id = 2L, name = "오리지널 스프", type = RecipeType.COMMON),
        )
        `when`(recipeRepository.findAllByOrderByIdAsc()).thenReturn(
            listOf(
                createRecipe(id = 1L, name = "따뜻한 입문 스프", type = RecipeType.COMMON, intro = true),
                createRecipe(id = 3L, name = "크리스탈 스프", type = RecipeType.HIDDEN, hidden = true),
                createRecipe(id = 4L, name = "숨은 일반 스프", type = RecipeType.COMMON),
            ),
        )

        val response = recipeQueryService.getRecipes(1L)

        assertEquals(1, response.introRecipes.size)
        assertEquals("오리지널 스프", response.weeklyRecipe?.name)
        assertEquals(1, response.hiddenRecipes.size)
    }

    @Test
    fun `공개 레시피 상세 조회 시 재료 목록을 반환한다`() {
        val recipe = createRecipe(id = 1L, name = "오리지널 스프", hidden = false)
        val ingredient1 = createIngredient(id = 1L, name = "양배추", type = IngredientType.COMMON)
        val ingredient2 = createIngredient(id = 2L, name = "토마토", type = IngredientType.COMMON)

        `when`(userRepository.findById(1L)).thenReturn(Optional.of(createUser()))
        `when`(userUnlockedRecipeRepository.findRecipeIdsByUserId(1L)).thenReturn(emptyList())
        `when`(recipeRepository.findById(1L)).thenReturn(Optional.of(recipe))
        `when`(recipeIngredientRepository.findAllByRecipeIdOrderBySlotOrderAsc(1L)).thenReturn(
            listOf(
                createRecipeIngredient(id = 1L, recipe = recipe, ingredient = ingredient1, slotOrder = 1),
                createRecipeIngredient(id = 2L, recipe = recipe, ingredient = ingredient2, slotOrder = 2),
            ),
        )

        val response = recipeQueryService.getRecipe(1L, 1L)

        assertEquals("오리지널 스프", response.name)
        assertTrue(response.recipeVisible)
        assertEquals(2, response.ingredients.size)
        assertEquals("양배추", response.ingredients[0].name)
    }

    @Test
    fun `비공개 레시피 상세 조회 시 재료 목록은 반환하지 않는다`() {
        val recipe = createRecipe(id = 2L, name = "스타라이트 스프", type = RecipeType.LEGENDARY, hidden = true)

        `when`(userRepository.findById(1L)).thenReturn(Optional.of(createUser()))
        `when`(userUnlockedRecipeRepository.findRecipeIdsByUserId(1L)).thenReturn(emptyList())
        `when`(recipeRepository.findById(2L)).thenReturn(Optional.of(recipe))

        val response = recipeQueryService.getRecipe(1L, 2L)

        assertEquals("???", response.name)
        assertEquals(false, response.recipeVisible)
        assertTrue(response.ingredients.isEmpty())
        verify(recipeIngredientRepository, never()).findAllByRecipeIdOrderBySlotOrderAsc(2L)
    }

    @Test
    fun `없는 레시피를 조회하면 예외가 발생한다`() {
        `when`(userRepository.findById(1L)).thenReturn(Optional.of(createUser()))
        `when`(userUnlockedRecipeRepository.findRecipeIdsByUserId(1L)).thenReturn(emptyList())
        `when`(recipeRepository.findById(999L)).thenReturn(Optional.empty())

        val exception = assertThrows<BusinessException> {
            recipeQueryService.getRecipe(1L, 999L)
        }

        assertEquals(ErrorCode.RECIPE_NOT_FOUND, exception.errorCode)
    }

    @Test
    fun `해금한 희귀 레시피는 목록과 상세에서 공개된다`() {
        val recipe = createRecipe(id = 2L, name = "크리스탈 스프", type = RecipeType.HIDDEN, hidden = true)
        val ingredient = createIngredient(id = 1L, name = "양배추", type = IngredientType.COMMON)

        `when`(userRepository.findById(1L)).thenReturn(Optional.of(createUser()))
        `when`(userUnlockedRecipeRepository.findRecipeIdsByUserId(1L)).thenReturn(listOf(2L))
        `when`(weeklyRecipeSelectionService.getCurrentWeeklyRecipe()).thenReturn(null)
        `when`(recipeRepository.findAllByOrderByIdAsc()).thenReturn(listOf(recipe))
        `when`(recipeRepository.findById(2L)).thenReturn(Optional.of(recipe))
        `when`(recipeIngredientRepository.findAllByRecipeIdOrderBySlotOrderAsc(2L)).thenReturn(
            listOf(createRecipeIngredient(id = 1L, recipe = recipe, ingredient = ingredient, slotOrder = 1)),
        )

        val listResponse = recipeQueryService.getRecipes(1L)
        val detailResponse = recipeQueryService.getRecipe(1L, 2L)

        assertEquals("크리스탈 스프", listResponse.hiddenRecipes[0].name)
        assertTrue(listResponse.hiddenRecipes[0].recipeVisible)
        assertEquals("크리스탈 스프", detailResponse.name)
        assertEquals(1, detailResponse.ingredients.size)
    }
}
