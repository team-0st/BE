package com.zerost.api.soup.application

import com.zerost.api.common.exception.BusinessException
import com.zerost.api.common.exception.ErrorCode
import com.zerost.api.ingredient.domain.UserIngredientRepository
import com.zerost.api.recipe.domain.Recipe
import com.zerost.api.recipe.domain.RecipeIngredient
import com.zerost.api.recipe.domain.RecipeIngredientRepository
import com.zerost.api.recipe.domain.RecipeRepository
import com.zerost.api.recipe.domain.RecipeType
import com.zerost.api.soup.domain.Soup
import com.zerost.api.soup.domain.SoupRepository
import com.zerost.api.support.createIngredient
import com.zerost.api.support.createUser
import com.zerost.api.support.createUserIngredient
import com.zerost.api.user.domain.UserRepository
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import java.util.Optional
import kotlin.test.assertEquals

class SoupBrewingServiceTest {

    private val userRepository = mock(UserRepository::class.java)
    private val recipeRepository = mock(RecipeRepository::class.java)
    private val recipeIngredientRepository = mock(RecipeIngredientRepository::class.java)
    private val userIngredientRepository = mock(UserIngredientRepository::class.java)
    private val soupRepository = mock(SoupRepository::class.java)
    private val soupBrewingService = SoupBrewingService(
        userRepository = userRepository,
        recipeRepository = recipeRepository,
        recipeIngredientRepository = recipeIngredientRepository,
        userIngredientRepository = userIngredientRepository,
        soupRepository = soupRepository,
    )

    @Test
    fun `순서가 일치하는 레시피를 찾으면 스프를 제작할 수 있다`() {
        val user = createUser()
        val ingredient1 = createIngredient(id = 1L, name = "양배추")
        val ingredient2 = createIngredient(id = 2L, name = "토마토")
        val ingredient3 = createIngredient(id = 3L, name = "양파")
        val recipe = Recipe(
            id = 1L,
            name = "오리지널 스프",
            type = RecipeType.COMMON,
            slotCount = 3,
            hidden = false,
        )
        val userIngredients = listOf(
            createUserIngredient(user = user, ingredient = ingredient1, quantity = 1),
            createUserIngredient(user = user, ingredient = ingredient2, quantity = 1),
            createUserIngredient(user = user, ingredient = ingredient3, quantity = 1),
        )

        `when`(userRepository.findByDeviceIdForUpdate("device-1")).thenReturn(Optional.of(user))
        `when`(recipeRepository.findAllBySlotCountOrderByIdAsc(3)).thenReturn(listOf(recipe))
        `when`(
            recipeIngredientRepository.findAllByRecipeIdInOrderByRecipeIdAscSlotOrderAsc(listOf(1L)),
        ).thenReturn(
            listOf(
                RecipeIngredient(id = 1L, recipe = recipe, ingredient = ingredient1, slotOrder = 1),
                RecipeIngredient(id = 2L, recipe = recipe, ingredient = ingredient2, slotOrder = 2),
                RecipeIngredient(id = 3L, recipe = recipe, ingredient = ingredient3, slotOrder = 3),
            ),
        )
        `when`(userIngredientRepository.findAllByUserIdAndIngredientIdIn(1L, listOf(1L, 2L, 3L))).thenReturn(userIngredients)
        `when`(soupRepository.save(any(Soup::class.java))).thenAnswer { invocation ->
            val soup = invocation.arguments[0] as Soup
            Soup(
                id = 10L,
                user = soup.user,
                recipe = soup.recipe,
            )
        }

        val response = soupBrewingService.brew("device-1", listOf(1L, 2L, 3L))

        assertEquals(10L, response.soupId)
        assertEquals(1L, response.recipeId)
        assertEquals("오리지널 스프", response.recipeName)
        assertEquals("COMMON", response.recipeType)
        assertEquals(0, userIngredients[0].quantity)
        assertEquals(0, userIngredients[1].quantity)
        assertEquals(0, userIngredients[2].quantity)
        verify(soupRepository).save(any(Soup::class.java))
    }

    @Test
    fun `재료 수가 3개 미만이면 제작할 수 없다`() {
        val exception = assertThrows<BusinessException> {
            soupBrewingService.brew("device-1", listOf(1L, 2L))
        }

        assertEquals(ErrorCode.INVALID_SOUP_SLOT_COUNT, exception.errorCode)
    }

    @Test
    fun `일치하는 레시피가 없으면 제작할 수 없다`() {
        val user = createUser()
        val recipe = Recipe(
            id = 1L,
            name = "오리지널 스프",
            type = RecipeType.COMMON,
            slotCount = 3,
            hidden = false,
        )
        val ingredient1 = createIngredient(id = 1L)
        val ingredient2 = createIngredient(id = 2L)
        val ingredient3 = createIngredient(id = 3L)

        `when`(userRepository.findByDeviceIdForUpdate("device-1")).thenReturn(Optional.of(user))
        `when`(recipeRepository.findAllBySlotCountOrderByIdAsc(3)).thenReturn(listOf(recipe))
        `when`(
            recipeIngredientRepository.findAllByRecipeIdInOrderByRecipeIdAscSlotOrderAsc(listOf(1L)),
        ).thenReturn(
            listOf(
                RecipeIngredient(id = 1L, recipe = recipe, ingredient = ingredient1, slotOrder = 1),
                RecipeIngredient(id = 2L, recipe = recipe, ingredient = ingredient2, slotOrder = 2),
                RecipeIngredient(id = 3L, recipe = recipe, ingredient = ingredient3, slotOrder = 3),
            ),
        )

        val exception = assertThrows<BusinessException> {
            soupBrewingService.brew("device-1", listOf(1L, 3L, 2L))
        }

        assertEquals(ErrorCode.SOUP_RECIPE_NOT_FOUND, exception.errorCode)
    }

    @Test
    fun `보유 재료 수량이 부족하면 제작할 수 없다`() {
        val user = createUser()
        val ingredient1 = createIngredient(id = 1L)
        val ingredient2 = createIngredient(id = 2L)
        val ingredient3 = createIngredient(id = 3L)
        val recipe = Recipe(
            id = 1L,
            name = "오리지널 스프",
            type = RecipeType.COMMON,
            slotCount = 3,
            hidden = false,
        )
        val userIngredients = listOf(
            createUserIngredient(user = user, ingredient = ingredient1, quantity = 1),
            createUserIngredient(user = user, ingredient = ingredient2, quantity = 0),
            createUserIngredient(user = user, ingredient = ingredient3, quantity = 1),
        )

        `when`(userRepository.findByDeviceIdForUpdate("device-1")).thenReturn(Optional.of(user))
        `when`(recipeRepository.findAllBySlotCountOrderByIdAsc(3)).thenReturn(listOf(recipe))
        `when`(
            recipeIngredientRepository.findAllByRecipeIdInOrderByRecipeIdAscSlotOrderAsc(listOf(1L)),
        ).thenReturn(
            listOf(
                RecipeIngredient(id = 1L, recipe = recipe, ingredient = ingredient1, slotOrder = 1),
                RecipeIngredient(id = 2L, recipe = recipe, ingredient = ingredient2, slotOrder = 2),
                RecipeIngredient(id = 3L, recipe = recipe, ingredient = ingredient3, slotOrder = 3),
            ),
        )
        `when`(userIngredientRepository.findAllByUserIdAndIngredientIdIn(1L, listOf(1L, 2L, 3L))).thenReturn(userIngredients)

        val exception = assertThrows<BusinessException> {
            soupBrewingService.brew("device-1", listOf(1L, 2L, 3L))
        }

        assertEquals(ErrorCode.INSUFFICIENT_INGREDIENT_QUANTITY, exception.errorCode)
    }
}
