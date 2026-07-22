package com.zerost.api.soup.domain

import org.springframework.data.jpa.repository.JpaRepository

interface SoupRerollPolicyIngredientRepository : JpaRepository<SoupRerollPolicyIngredient, Long> {
    fun findAllBySoupRerollPolicyCandidateIdInOrderByIdAsc(
        soupRerollPolicyCandidateIds: List<Long>,
    ): List<SoupRerollPolicyIngredient>
}
