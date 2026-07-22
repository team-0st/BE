package com.zerost.api.soup.domain

import org.springframework.data.jpa.repository.JpaRepository

interface SoupBonusRewardPolicyIngredientRepository : JpaRepository<SoupBonusRewardPolicyIngredient, Long> {
    fun findAllBySoupBonusRewardPolicyIdInOrderByIdAsc(soupBonusRewardPolicyIds: List<Long>): List<SoupBonusRewardPolicyIngredient>
}
