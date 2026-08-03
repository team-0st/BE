package com.zerost.api.recipe.domain

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface UserUnlockedRecipeRepository : JpaRepository<UserUnlockedRecipe, Long> {

    @Query("select uur.recipe.id from UserUnlockedRecipe uur where uur.user.id = :userId")
    fun findRecipeIdsByUserId(@Param("userId") userId: Long): List<Long>
}
