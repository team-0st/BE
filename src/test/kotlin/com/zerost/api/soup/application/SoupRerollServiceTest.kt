package com.zerost.api.soup.application

import com.zerost.api.common.exception.BusinessException
import com.zerost.api.common.exception.ErrorCode
import com.zerost.api.ecojam.domain.EcoJamHistoryRepository
import com.zerost.api.ingredient.domain.IngredientHistoryRepository
import com.zerost.api.ingredient.domain.IngredientRepository
import com.zerost.api.ingredient.domain.IngredientType
import com.zerost.api.ingredient.domain.UserIngredientRepository
import com.zerost.api.point.domain.PointHistoryRepository
import com.zerost.api.recipe.domain.RecipeType
import com.zerost.api.soup.domain.SoupRepository
import com.zerost.api.soup.domain.SoupRewardGrade
import com.zerost.api.soup.domain.SoupRewardIngredientRepository
import com.zerost.api.support.createIngredient
import com.zerost.api.support.createRecipe
import com.zerost.api.support.createSoup
import com.zerost.api.support.createSoupRewardIngredient
import com.zerost.api.support.createUser
import com.zerost.api.support.createUserIngredient
import com.zerost.api.user.domain.UserRepository
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import java.util.Optional
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SoupRerollServiceTest {

    private val userRepository = mock(UserRepository::class.java)
    private val soupRepository = mock(SoupRepository::class.java)
    private val ingredientRepository = mock(IngredientRepository::class.java)
    private val userIngredientRepository = mock(UserIngredientRepository::class.java)
    private val soupRewardIngredientRepository = mock(SoupRewardIngredientRepository::class.java)
    private val ingredientHistoryRepository = mock(IngredientHistoryRepository::class.java)
    private val ecoJamHistoryRepository = mock(EcoJamHistoryRepository::class.java)
    private val pointHistoryRepository = mock(PointHistoryRepository::class.java)
    private val randomProvider = mock(RandomProvider::class.java)
    private val soupRewardService = SoupRewardService(
        ingredientRepository = ingredientRepository,
        userIngredientRepository = userIngredientRepository,
        soupRewardIngredientRepository = soupRewardIngredientRepository,
        ingredientHistoryRepository = ingredientHistoryRepository,
        ecoJamHistoryRepository = ecoJamHistoryRepository,
        pointHistoryRepository = pointHistoryRepository,
        randomProvider = randomProvider,
    )
    private val soupRerollService = SoupRerollService(
        userRepository = userRepository,
        soupRepository = soupRepository,
        soupRewardService = soupRewardService,
        ecoJamHistoryRepository = ecoJamHistoryRepository,
    )

    @Test
    fun `일반 스프 꽝 보상을 리롤하면 기존 보상을 회수하고 새 보상을 적용한다`() {
        val user = createUser(ecoJam = 130, point = 0)
        val recipe = createRecipe(type = RecipeType.COMMON)
        val soup = createSoup(
            id = 10L,
            user = user,
            recipe = recipe,
            rewardGrade = SoupRewardGrade.CONSOLATION,
            rewardEcoJam = 30,
            rewardPoint = 0,
        )

        `when`(soupRepository.findById(10L)).thenReturn(Optional.of(soup))
        `when`(userRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(user))
        `when`(soupRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(soup))
        `when`(soupRewardIngredientRepository.findAllBySoupIdOrderByIdAsc(10L)).thenReturn(emptyList())
        `when`(randomProvider.nextInt(100)).thenReturn(65)

        val response = soupRerollService.reroll(1L, 10L)

        assertEquals(10L, response.soupId)
        assertEquals(30, response.rerollCostEcoJam)
        assertEquals("SMALL", response.rewardGrade)
        assertEquals(0, response.rewardEcoJam)
        assertEquals(500, response.rewardPoint)
        assertEquals(70, response.remainingEcoJam)
        assertEquals(500, user.point)
        assertEquals(70, user.ecoJam)
        assertTrue(soup.rerolled)
        verify(ecoJamHistoryRepository, org.mockito.Mockito.atLeastOnce()).save(any())
        verify(pointHistoryRepository, org.mockito.Mockito.atLeastOnce()).save(any())
    }

    @Test
    fun `재료 보상을 리롤하면 기존 지급 재료를 회수하고 새 재료 보상을 적용한다`() {
        val user = createUser(ecoJam = 180)
        val recipe = createRecipe(type = RecipeType.COMMON)
        val ingredient = createIngredient(id = 1L, name = "양배추", type = IngredientType.COMMON)
        val newIngredient = createIngredient(id = 2L, name = "토마토", type = IngredientType.COMMON)
        val soup = createSoup(
            id = 10L,
            user = user,
            recipe = recipe,
            rewardGrade = SoupRewardGrade.INGREDIENT,
            rewardEcoJam = 50,
            rewardPoint = 0,
        )
        val rewardedIngredient = createSoupRewardIngredient(
            soup = soup,
            ingredient = ingredient,
            quantity = 1,
        )
        val existingUserIngredient = createUserIngredient(user = user, ingredient = ingredient, quantity = 1)

        `when`(soupRepository.findById(10L)).thenReturn(Optional.of(soup))
        `when`(userRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(user))
        `when`(soupRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(soup))
        `when`(soupRewardIngredientRepository.findAllBySoupIdOrderByIdAsc(10L)).thenReturn(listOf(rewardedIngredient))
        `when`(userIngredientRepository.findByUserAndIngredient(user, ingredient)).thenReturn(Optional.of(existingUserIngredient))
        `when`(ingredientRepository.findAllByType(IngredientType.COMMON)).thenReturn(listOf(newIngredient))
        `when`(userIngredientRepository.findByUserAndIngredient(user, newIngredient)).thenReturn(Optional.empty())
        `when`(randomProvider.nextInt(100)).thenReturn(0, 0)

        val response = soupRerollService.reroll(1L, 10L)

        assertEquals("INGREDIENT", response.rewardGrade)
        assertEquals(50, response.rewardEcoJam)
        assertEquals(0, response.rewardPoint)
        assertEquals(130, response.remainingEcoJam)
        assertEquals(0, existingUserIngredient.quantity)
        verify(userIngredientRepository).save(any())
        verify(ingredientHistoryRepository, org.mockito.Mockito.atLeastOnce()).save(any())
    }

    @Test
    fun `이미 리롤한 스프는 다시 리롤할 수 없다`() {
        val user = createUser(ecoJam = 500)
        val soup = createSoup(
            id = 10L,
            user = user,
            recipe = createRecipe(type = RecipeType.COMMON),
            rewardGrade = SoupRewardGrade.SMALL,
            rewardPoint = 500,
            rerolled = true,
        )

        `when`(soupRepository.findById(10L)).thenReturn(Optional.of(soup))
        `when`(userRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(user))
        `when`(soupRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(soup))

        val exception = assertThrows<BusinessException> {
            soupRerollService.reroll(1L, 10L)
        }

        assertEquals(ErrorCode.SOUP_REROLL_ALREADY_COMPLETED, exception.errorCode)
        verify(ecoJamHistoryRepository, never()).save(any())
    }

    @Test
    fun `기존 보상을 회수할 수 없으면 리롤 비용 차감 전에 예외가 발생한다`() {
        val user = createUser(ecoJam = 20, point = 0)
        val soup = createSoup(
            id = 10L,
            user = user,
            recipe = createRecipe(type = RecipeType.COMMON),
            rewardGrade = SoupRewardGrade.CONSOLATION,
            rewardEcoJam = 30,
            rewardPoint = 0,
        )

        `when`(soupRepository.findById(10L)).thenReturn(Optional.of(soup))
        `when`(userRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(user))
        `when`(soupRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(soup))
        `when`(soupRewardIngredientRepository.findAllBySoupIdOrderByIdAsc(10L)).thenReturn(emptyList())

        val exception = assertThrows<BusinessException> {
            soupRerollService.reroll(1L, 10L)
        }

        assertEquals(ErrorCode.SOUP_REROLL_REWARD_RECOVERY_NOT_AVAILABLE, exception.errorCode)
        verify(ecoJamHistoryRepository, never()).save(any())
    }
}
