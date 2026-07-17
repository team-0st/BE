package com.zerost.api.ingredient.domain

import org.springframework.data.jpa.repository.JpaRepository

interface IngredientRepository : JpaRepository<Ingredient, Long> {
    fun findFirstByOrderByIdAsc(): Ingredient?
}
