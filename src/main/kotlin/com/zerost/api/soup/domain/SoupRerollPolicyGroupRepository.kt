package com.zerost.api.soup.domain

import com.zerost.api.recipe.domain.RecipeType
import java.util.Optional
import org.springframework.data.jpa.repository.JpaRepository

interface SoupRerollPolicyGroupRepository : JpaRepository<SoupRerollPolicyGroup, Long> {
    fun findByRecipeTypeAndCurrentRewardGradeAndActiveTrue(
        recipeType: RecipeType,
        currentRewardGrade: SoupRewardGrade,
    ): Optional<SoupRerollPolicyGroup>
}
