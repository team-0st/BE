package com.zerost.api.soup.domain

import org.springframework.data.jpa.repository.JpaRepository

interface SoupRewardPolicyIngredientRepository : JpaRepository<SoupRewardPolicyIngredient, Long> {
    fun findAllBySoupRewardPolicyIdInOrderByIdAsc(soupRewardPolicyIds: List<Long>): List<SoupRewardPolicyIngredient>
}
