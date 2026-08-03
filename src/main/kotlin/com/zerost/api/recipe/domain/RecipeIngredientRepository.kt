package com.zerost.api.recipe.domain

import org.springframework.data.jpa.repository.JpaRepository

interface RecipeIngredientRepository : JpaRepository<RecipeIngredient, Long> {
    fun findAllByRecipeIdOrderBySlotOrderAsc(recipeId: Long): List<RecipeIngredient>
    fun findAllByRecipeIdInOrderByRecipeIdAscSlotOrderAsc(recipeIds: Collection<Long>): List<RecipeIngredient>
}
