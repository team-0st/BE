package com.zerost.api.ingredient.domain

import com.zerost.api.support.createUserIngredient
import kotlin.test.Test
import kotlin.test.assertEquals

class UserIngredientTest {

    @Test
    fun `재료 수량 증가 시 기본값은 1만큼 증가한다`() {
        val userIngredient = createUserIngredient(quantity = 2)

        userIngredient.increaseQuantity()

        assertEquals(3, userIngredient.quantity)
    }

    @Test
    fun `재료 수량 증가 시 전달한 수량만큼 증가한다`() {
        val userIngredient = createUserIngredient(quantity = 2)

        userIngredient.increaseQuantity(amount = 3)

        assertEquals(5, userIngredient.quantity)
    }
}
