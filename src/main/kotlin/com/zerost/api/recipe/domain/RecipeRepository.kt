package com.zerost.api.recipe.domain

import org.springframework.data.jpa.repository.JpaRepository

interface RecipeRepository : JpaRepository<Recipe, Long> {
    fun findAllByOrderByIdAsc(): List<Recipe>
    fun findAllByTypeOrderByIdAsc(type: RecipeType): List<Recipe>
    fun findAllBySlotCountOrderByIdAsc(slotCount: Int): List<Recipe>
}
