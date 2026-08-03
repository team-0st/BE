package com.zerost.api.ingredient.application

import com.zerost.api.ingredient.domain.UserIngredientRepository
import com.zerost.api.support.createIngredient
import com.zerost.api.support.createUser
import com.zerost.api.support.createUserIngredient
import com.zerost.api.user.domain.UserRepository
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import java.util.Optional
import kotlin.test.assertEquals

class UserIngredientQueryServiceTest {

    private val userRepository = mock(UserRepository::class.java)
    private val userIngredientRepository = mock(UserIngredientRepository::class.java)
    private val userIngredientQueryService = UserIngredientQueryService(userRepository, userIngredientRepository)

    @Test
    fun `보유 재료 목록 조회 시 재료 식별자와 수량을 반환한다`() {
        val user = createUser()
        val ingredient = createIngredient(id = 7L, name = "낡은 밧줄")
        val userIngredient = createUserIngredient(
            id = 100L,
            user = user,
            ingredient = ingredient,
            quantity = 3,
        )
        `when`(userRepository.findById(1L)).thenReturn(Optional.of(user))
        `when`(userIngredientRepository.findAllByUserIdOrderByIdAsc(1L)).thenReturn(listOf(userIngredient))

        val response = userIngredientQueryService.getUserIngredients(1L)

        assertEquals(1, response.size)
        assertEquals(7L, response[0].ingredientId)
        assertEquals("낡은 밧줄", response[0].name)
        assertEquals(3, response[0].quantity)
    }
}
