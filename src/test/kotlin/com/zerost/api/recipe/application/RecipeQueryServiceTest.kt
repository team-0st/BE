package com.zerost.api.recipe.application

import com.zerost.api.common.exception.BusinessException
import com.zerost.api.common.exception.ErrorCode
import com.zerost.api.ingredient.domain.IngredientType
import com.zerost.api.recipe.domain.RecipeIngredientRepository
import com.zerost.api.recipe.domain.RecipeRepository
import com.zerost.api.recipe.domain.RecipeType
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
    private val recipeQueryService = RecipeQueryService(
        userRepository = userRepository,
        recipeRepository = recipeRepository,
        recipeIngredientRepository = recipeIngredientRepository,
    )

    @Test
    fun `레시피 목록 조회 시 비공개 레시피 이름은 마스킹된다`() {
        `when`(userRepository.findByDeviceId("device-1")).thenReturn(Optional.of(createUser()))
        `when`(recipeRepository.findAllByOrderByIdAsc()).thenReturn(
            listOf(
                createRecipe(id = 1L, name = "오리지널 스프", type = RecipeType.COMMON, hidden = false),
                createRecipe(id = 2L, name = "크리스탈 스프", type = RecipeType.HIDDEN, hidden = true),
            ),
        )

        val response = recipeQueryService.getRecipes("device-1")

        assertEquals(2, response.size)
        assertEquals("오리지널 스프", response[0].name)
        assertTrue(response[0].recipeVisible)
        assertEquals("???", response[1].name)
        assertEquals("HIDDEN", response[1].type)
        assertEquals(false, response[1].recipeVisible)
    }

    @Test
    fun `공개 레시피 상세 조회 시 재료 목록을 반환한다`() {
        val recipe = createRecipe(id = 1L, name = "오리지널 스프", hidden = false)
        val ingredient1 = createIngredient(id = 1L, name = "양배추", type = IngredientType.COMMON)
        val ingredient2 = createIngredient(id = 2L, name = "토마토", type = IngredientType.COMMON)

        `when`(userRepository.findByDeviceId("device-1")).thenReturn(Optional.of(createUser()))
        `when`(recipeRepository.findById(1L)).thenReturn(Optional.of(recipe))
        `when`(recipeIngredientRepository.findAllByRecipeIdOrderBySlotOrderAsc(1L)).thenReturn(
            listOf(
                createRecipeIngredient(id = 1L, recipe = recipe, ingredient = ingredient1, slotOrder = 1),
                createRecipeIngredient(id = 2L, recipe = recipe, ingredient = ingredient2, slotOrder = 2),
            ),
        )

        val response = recipeQueryService.getRecipe("device-1", 1L)

        assertEquals("오리지널 스프", response.name)
        assertTrue(response.recipeVisible)
        assertEquals(2, response.ingredients.size)
        assertEquals("양배추", response.ingredients[0].name)
    }

    @Test
    fun `비공개 레시피 상세 조회 시 재료 목록은 반환하지 않는다`() {
        val recipe = createRecipe(id = 2L, name = "스타라이트 스프", type = RecipeType.LEGENDARY, hidden = true)

        `when`(userRepository.findByDeviceId("device-1")).thenReturn(Optional.of(createUser()))
        `when`(recipeRepository.findById(2L)).thenReturn(Optional.of(recipe))

        val response = recipeQueryService.getRecipe("device-1", 2L)

        assertEquals("???", response.name)
        assertEquals(false, response.recipeVisible)
        assertTrue(response.ingredients.isEmpty())
        verify(recipeIngredientRepository, never()).findAllByRecipeIdOrderBySlotOrderAsc(2L)
    }

    @Test
    fun `없는 레시피를 조회하면 예외가 발생한다`() {
        `when`(userRepository.findByDeviceId("device-1")).thenReturn(Optional.of(createUser()))
        `when`(recipeRepository.findById(999L)).thenReturn(Optional.empty())

        val exception = assertThrows<BusinessException> {
            recipeQueryService.getRecipe("device-1", 999L)
        }

        assertEquals(ErrorCode.RECIPE_NOT_FOUND, exception.errorCode)
    }
}
