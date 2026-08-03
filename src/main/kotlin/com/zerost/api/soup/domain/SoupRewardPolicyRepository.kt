package com.zerost.api.soup.domain

import com.zerost.api.recipe.domain.RecipeType
import org.springframework.data.jpa.repository.JpaRepository

interface SoupRewardPolicyRepository : JpaRepository<SoupRewardPolicy, Long> {
    fun findAllByRecipeTypeAndIntroOnlyAndActiveTrueOrderByIdAsc(
        recipeType: RecipeType,
        introOnly: Boolean,
    ): List<SoupRewardPolicy>
}
