package com.zerost.api.soup.domain

import com.zerost.api.recipe.domain.RecipeType
import org.springframework.data.jpa.repository.JpaRepository

interface SoupBonusRewardPolicyRepository : JpaRepository<SoupBonusRewardPolicy, Long> {
    fun findAllByRecipeTypeAndActiveTrueOrderByIdAsc(recipeType: RecipeType): List<SoupBonusRewardPolicy>
}
