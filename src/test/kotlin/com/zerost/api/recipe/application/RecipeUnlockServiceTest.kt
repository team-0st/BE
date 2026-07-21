package com.zerost.api.recipe.application

import com.zerost.api.common.exception.BusinessException
import com.zerost.api.common.exception.ErrorCode
import com.zerost.api.ecojam.domain.EcoJamHistoryRepository
import com.zerost.api.recipe.domain.RecipeRepository
import com.zerost.api.recipe.domain.RecipeType
import com.zerost.api.recipe.domain.UserUnlockedRecipeRepository
import com.zerost.api.support.createRecipe
import com.zerost.api.support.createUser
import com.zerost.api.user.domain.UserRepository
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import java.util.Optional
import kotlin.test.assertEquals

class RecipeUnlockServiceTest {

    private val userRepository = mock(UserRepository::class.java)
    private val recipeRepository = mock(RecipeRepository::class.java)
    private val userUnlockedRecipeRepository = mock(UserUnlockedRecipeRepository::class.java)
    private val ecoJamHistoryRepository = mock(EcoJamHistoryRepository::class.java)
    private val recipeUnlockRandomProvider = mock(RecipeUnlockRandomProvider::class.java)
    private val recipeUnlockService = RecipeUnlockService(
        userRepository = userRepository,
        recipeRepository = recipeRepository,
        userUnlockedRecipeRepository = userUnlockedRecipeRepository,
        ecoJamHistoryRepository = ecoJamHistoryRepository,
        recipeUnlockRandomProvider = recipeUnlockRandomProvider,
    )

    @Test
    fun `희귀 레시피 랜덤 해금 시 미해금 레시피 중 하나를 해금하고 에코잼을 차감한다`() {
        val user = createUser(ecoJam = 800)
        val recipe1 = createRecipe(id = 1L, name = "크리스탈 스프", type = RecipeType.HIDDEN, hidden = true)
        val recipe2 = createRecipe(id = 2L, name = "포레스트 스프", type = RecipeType.HIDDEN, hidden = true)

        `when`(userRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(user))
        `when`(recipeRepository.findAllByTypeOrderByIdAsc(RecipeType.HIDDEN)).thenReturn(listOf(recipe1, recipe2))
        `when`(userUnlockedRecipeRepository.findRecipeIdsByUserId(1L)).thenReturn(listOf(1L))
        `when`(recipeUnlockRandomProvider.nextInt(1)).thenReturn(0)

        val response = recipeUnlockService.unlockRandomHiddenRecipe(1L)

        assertEquals(2L, response.recipeId)
        assertEquals("포레스트 스프", response.recipeName)
        assertEquals(300, response.remainingEcoJam)
        verify(userUnlockedRecipeRepository).save(any())
        verify(ecoJamHistoryRepository).save(any())
    }

    @Test
    fun `모든 희귀 레시피를 이미 해금했다면 예외가 발생한다`() {
        val user = createUser(ecoJam = 800)
        val recipe1 = createRecipe(id = 1L, name = "크리스탈 스프", type = RecipeType.HIDDEN, hidden = true)
        val recipe2 = createRecipe(id = 2L, name = "포레스트 스프", type = RecipeType.HIDDEN, hidden = true)

        `when`(userRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(user))
        `when`(recipeRepository.findAllByTypeOrderByIdAsc(RecipeType.HIDDEN)).thenReturn(listOf(recipe1, recipe2))
        `when`(userUnlockedRecipeRepository.findRecipeIdsByUserId(1L)).thenReturn(listOf(1L, 2L))

        val exception = assertThrows<BusinessException> {
            recipeUnlockService.unlockRandomHiddenRecipe(1L)
        }

        assertEquals(ErrorCode.ALL_HIDDEN_RECIPES_ALREADY_UNLOCKED, exception.errorCode)
    }
}
