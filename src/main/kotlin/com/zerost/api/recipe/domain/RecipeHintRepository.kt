package com.zerost.api.recipe.domain

import org.springframework.data.jpa.repository.JpaRepository

interface RecipeHintRepository : JpaRepository<RecipeHint, Long> {
    fun findAllByRecipeIdInOrderByRecipeIdAscIdAsc(recipeIds: List<Long>): List<RecipeHint>
    fun findAllByRecipeIdOrderByIdAsc(recipeId: Long): List<RecipeHint>
}
